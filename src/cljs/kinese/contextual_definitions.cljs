(ns kinese.contextual-definitions
  (:require [reagent.core :as reagent]))

(defn token-div [i [word definition] show-level]
  "Creates a context-aware word-definition(s) div.
   `i` is a function (containing some `reagent/atom`s) that returns
   the number of definition-lines printed *before* the token, i.e.
   the height (in em) at which to print it.
   `show-level` is a `reagent/atom` of possible values [:none :first :all]
   `definition` is a collection of strings"
  [:div.token ^{:key i}
   [:div.word
    [:a
     {:on-click (fn [] (reset! show-level (if (<= (count definition) 1)
                                            (case @show-level
                                              :none :first
                                              :first :none)
                                            (case @show-level
                                              :none :first
                                              :first :all
                                              :all :none))))}
     [:h1.is-size-3 word]]]
   [:div.definition
    {:style {:margin-top (str (* (i) 1.5) "em")}}
    [:em 
     (when (not= @show-level :none)
       (if (= @show-level :all)
         (into [:ul] (map (fn [i] [:li i]) definition))
         (first definition)))]]])

(defn contextual-definitions [dictionary]
  (into [:div.container.textcontainer.is-flex]
        (loop [i 0
               tokens []
               atoms []
               dict-list dictionary]
          (let [current-atom (reagent/atom :first)]
            (println (map #(first (nth % 2)) tokens))
            (if (or (= i 10) (empty? dict-list))
              tokens
              (recur (inc i)
                     (conj tokens [token-div
                                   #(apply + (map (fn [tok ato]
                                                    (case @ato
                                                      :none 0
                                                      :first 1
                                                      :all (->> (nth tok 2) second count)))
                                                  tokens
                                                  atoms))
                                   (first dict-list)
                                   current-atom])
                     (conj atoms current-atom)
                     (rest dict-list)))))))
