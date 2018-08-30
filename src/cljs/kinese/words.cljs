(ns kinese.words
  (:require [ajax.core :refer [POST]]))

(defn create-word-map
  "Transforms the POST response in the more useful format used internally"
  [words dict]
  (map (fn [word]
         (let [entry (dict word)
               definition (map :definition entry)
               characters (:pronunciation (first entry))]
           {:definition definition
            :characters characters
            :text word}))
       words))

(defn post
  "Performs a POST request to /kar with :params `params` and calls `handler` with a word map as returned by `create-word-map`"
  [params handler]
  (POST "/kar" {:params params
                :response-format :json
                :keywords? true
                :handler (fn [response]
                           (handler (create-word-map
                                     (:segmented-text response)
                                     (reduce-kv #(assoc %1 (name %2) %3) {}
                                                (:words response)))))}))

