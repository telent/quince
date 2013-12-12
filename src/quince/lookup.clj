(ns quince.lookup
  (:require [quince.btree :as bt]
            [clojure.core.reducers :as r])
  (:use [midje.sweet]))

(def index-message-ids (r/map (fn [m] [(:message-id m) m])))

(facts "about lookups"
       (let [docs 
             [{:message-id "hello" :attribute 1 :spoon "bent"}
              {:message-id "goodbye" :attribute 2 :spoon "runcible"}]
             index (r/reduce bt/tree-insert nil (index-message-ids docs))]
         (fact "the message-id reducer generates a btree of parsed messages keyed on message-id"
               (bt/height index) => 2
               (:spoon (bt/tree-lookup index "hello")) => "bent")))
