(ns quince.mime
  [:require
   clojure.java.io
   [quince.interop :as i]
   [clojure.string :as str]
   [clojure.set :as set]]
  (:use [midje.sweet])
  [:import
   [org.apache.james.mime4j.dom MessageServiceFactory]
   [org.apache.james.mime4j.dom.address Mailbox]
   [org.apache.james.mime4j.stream MimeConfig]])

(defn extract-address [address]
  (cond (nil? address)
        nil
        (instance? Mailbox address)
        {
         :address (. address getAddress)
         :name (. address getName)
         }
        :else
        {
         :name (. address toString)
         }))

(defn extract-addresses [addresses]
  (if (seq addresses)
    (map extract-address addresses)))

(defn extract-header [message header-name]
  (map bean (seq (.getFields (.getHeader message) header-name))))

(defn extract-references [message & [header]]
  (let [refs (extract-header message (or header "References"))
        split-on-space  #(clojure.string/split (:value %) #"\s+")]
    (into #{} (mapcat split-on-space refs) )))

(declare extract-message)
(defn extract-body [message]
  ;; if the content-type is multipart, the body is a seq of parts.
  ;; Each of the parts is a hash which looks a lot like a message, but
  ;; with different/more/fewer keys
  (let [body (.getBody message)]
    (cond
     (instance? org.apache.james.mime4j.dom.Multipart body)
     (map extract-message (.getBodyParts body))

     (instance? org.apache.james.mime4j.dom.TextBody  body)
     (slurp (.getReader body))

     :else
     {:unknown (.toString (.getClass body))})))

(defn extract-safe-headers [message]
  (let [safe (select-keys (bean message) [:subject :dispositionType :mimeType
                                          :messageId :charset :inReplyTo
                                          :contentTransferEncoding :date])]
    (zipmap (map i/from-lower-camel-case (keys safe)) (vals safe))))

(defn extract-convertable-headers [message]
  (let [mbean (bean message)]
    {:to (extract-addresses (get mbean :to nil))
     :from (extract-addresses (get mbean :from nil))
     :sender (extract-address (get mbean :sender nil))
     :reply-to (extract-addresses (get mbean :replyTo nil))
     :cc (extract-addresses (get mbean :cc nil))
     :body (extract-body message)
     :references (extract-references message)
     :in-reply-to (extract-references message "In-Reply-To")
     :content-type
     (map keyword (clojure.string/split
                   (get mbean :mimeType nil) #"/"))
     }))


(defn extract-message [message]
  (into {} (filter second
                   (merge (extract-safe-headers message)
                          (extract-convertable-headers message)))))

(defn james-parse-message [file]
  (let [config (let [c (MimeConfig.)]
                 (. c setMaxLineLen 0)
                 (. c setMaxHeaderCount 0)
                 (. c setMaxHeaderLen 0)
                 c)
        builder (let [b (. (. MessageServiceFactory (newInstance))
                           newMessageBuilder)]
                  (. b setMimeEntityConfig config)
                  b)
        stream (clojure.java.io/input-stream file)]
    (. builder parseMessage stream)))

(defn read-message [file]
  (let [message (james-parse-message file)]
    (if (zero? (count (.getFields (.getHeader message))))
      nil
      (extract-message message))))

(facts "about read-message"
       (let [dir (System/getProperty "user.dir")
             msg (read-message (java.io.File. dir "fixtures/nnml/1.msg"))]
         (:subject msg) => "Failing on load-foreign function"
         (:message-id msg) => "<4B41F76D.50106@gmail.com>"
         (:references msg) => #{"<9552de430912310205u72ef9c31o2dc5afbc16d6a234@mail.gmail.com>"
                                "<4B3CB2C6.3000309@gmail.com>"
                                "<9552de430912311031y6bd55ec2x7c5159002c290b67@mail.gmail.com>"}
         ;; you might not have known this, but in-reply-to can point to
         ;; > 1 message-id
         (:in-reply-to msg) => #{"<9552de430912311031y6bd55ec2x7c5159002c290b67@mail.gmail.com>"}
         )

       (let [non-msg (read-message "/dev/null")]
         non-msg => nil))
