(ns quince.interop
  [:require
   [clojure.string :as str]
   [clojure.set :as set]]
  (:use [midje.sweet]))

(defn match-repeatedly [matcher]
  (when (.find matcher)
    (let [el [(.start matcher) (.end matcher)]]
      (conj (match-repeatedly matcher) el))))

(defn from-lower-camel-case [camelcased]
  (let [strng (name camelcased)
        downup (match-repeatedly (re-matcher #"[\P{Lu}][\p{Lu}]+" strng))
        updown (match-repeatedly (re-matcher #"[\p{Lu}][\P{Lu}]+" strng))
        boundaries (sort (set/union
                          (set [(.length strng)])
                          (set (map #(inc (first %)) downup))
                          (set (map #(first %) updown))))]
    (keyword
     (clojure.string/join
      "-"
      (map #(.toLowerCase (subs strng %1 %2))
           (conj boundaries 0) boundaries)))))

(facts "turns javaish keyword into something civilised"
       (from-lower-camel-case :helloWorld) => :hello-world
       (from-lower-camel-case :messageID) => :message-id
       (from-lower-camel-case :nextURLMatcher) => :next-url-matcher
       )
