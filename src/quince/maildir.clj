(ns quince.maildir
  (:use [midje.sweet]))

(defn maildir-files-seq [dir]
  (let [files (file-seq (clojure.java.io/file dir))]
    (filter #(and (.isFile %)
                  (re-find #"^([0-9]+)\.[MP]([0-9]+)" (.getName %)))
            files)))

(facts "maildir-files-seq"
       (let [dir (System/getProperty "user.dir")
             s (maildir-files-seq (java.io.File. dir "fixtures/nnml/"))]
         (fact "finds 56 messages"
               (count s) => 56)
         (fact "returns regular files"
               s => (partial every? #(.isFile %)))))
