(ns kinese.lang.chinese.dictionary
  (:require [kinese.lang.chinese.hsk :refer [hsk]]
            [clojure.string :as string]))

(defn add-hsk-metadata [dict hsk]
  (reduce-kv (fn [acc-dict level words]
            (reduce (fn [di entry]
                         (assoc di entry (map #(assoc %1 :level (inc level)) (di entry))))
                    acc-dict
                    (string/split words #" ")))
          dict hsk))

(defn read-dict-inner [path]
  (let [txt (slurp path)]
    (reduce (fn [dict [word entry]]
              (let [[raw-pronunciation definition] entry
                    pronunciation (map #(hash-map :pinyin (apply str (drop-last %))
                                                  :tone (last %))
                                       (string/split raw-pronunciation #" "))]
                (merge-with into dict {word [{:definition definition
                                              :pronunciation pronunciation}]})))
            {}
            (map
             #(let [t (next %)]
                [(first t) (next t)]) ;( vector (ffirst %2) (nnext %2))) 
             (re-seq #"(?m)^.* (.*) \[((?:\w+\d\s?)+)\] \/(.*)\/$" txt)))))

(defn read-dict [path]
  (add-hsk-metadata (read-dict-inner path) hsk))
