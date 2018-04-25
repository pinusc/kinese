(ns kinese.dict-serialize
  (:require [clojure.string :as string]))


(defn read-dict [path]
  (let [txt (slurp path)]
    (into {}
          (reduce (fn [li n] 
                    (do 
                      ;(when (< (count li) 10) 
                        ;(let [term (first n)
                        ;      tone (map #(vector (apply str (drop-last %)) (last %)) (string/split (first (fnext n)) #" "))
                        ;      definition (fnext (fnext n))]
                        ;  (println "n: " (string/split (first (fnext n)) #" "))
                        ;  (println "first n: " (first n))
                        ;  (println "len n" (count n))
                        ;  (println "")
                        ;  (println "")
                        ;  (println "############" )
                        ;  (println "---------TERM-----------" term)
                        ;  (println "------DEFINITION--------" definition)
                        ;  (println "---------TONE-----------" tone)
                        ;  (println "------TONE-TYPE---------" (type tone))
                        ;  (println "@@@@@@@@@@@@" )
                        ;  (println "")
                        ;  (println "")
                        ;  (println "N: " (conj (fnext (first li))))))
                        (let [term (first n)
                              tone (map #(vector (apply str (drop-last %)) (last %)) (string/split (first (fnext n)) #" "))
                              definition (fnext (fnext n))]
                          (if (= term (ffirst li))
                            (conj (next li) [term (conj (fnext (first li)) [tone definition])]) ; concatenate new definition to existing one
                            ;(conj (next li) [(first n) (next (first li))])
                            (conj li [term [[tone definition]]]))))) ; create new definition
                  '()
                  (map
                    #(let [t (next %)]
                       [(first t) (next t)]) ;( vector (ffirst %2) (nnext %2))) 
                    (re-seq #"(?m)^.* (.*) \[((?:\w+\d\s?)+)\] \/(.*)\/$" txt))))))
