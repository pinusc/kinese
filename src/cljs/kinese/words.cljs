(ns kinese.words
  (:require [ajax.core :refer [GET]]))

(defn create-word-map
  "Transforms the POST response in the more useful format used internally"
  [words dict]
  (map (fn [word]
         (let [entry (dict word)
               definition (map :definition entry)
               characters (:pronunciation (first entry))]
           {:definition definition
            :level (or (:level (first entry)) 7) ;; max level is 6, 7 means not classified
            :characters characters
            :text word}))
       words))

(defn get-words
  "Performs a POST request to /kar with :params `params` and calls `handler` with a word map as returned by `create-word-map`"
  [params handler]
  (GET "/api/kar" {:params params
                   :response-format :json
                   :keywords? true
                   :handler (fn [response]
                              (handler (create-word-map
                                        (:segmented-text response)
                                        (reduce-kv #(assoc %1 (name %2) %3) {}
                                                   (:words response)))))}))

