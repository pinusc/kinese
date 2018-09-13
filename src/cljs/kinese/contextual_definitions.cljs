(ns kinese.contextual-definitions
  (:require-macros [kinese.logging :refer [log info]])
  (:require [reagent.core :as reagent]
            [ajax.core :refer [GET]]
            [kinese.floating-menu :refer [floating-menu]]
            [kinese.data :refer [state]]
            [kinese.components]
            [secretary.core :as secretary :include-macros true]
            [kinese.words :as words]))
;; (def wikiurl  "https://zh.wikipedia.org/w/api.php?action=query&format=json&origin=*&generator=random&prop=extracts&exlimit=1&exchars=200&exintro=true&explaintext=true")
  (def wikiurl  "https://zh.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&origin=*&generator=random")

(defn get-random-text
  [handler]
  (GET wikiurl
       :handler (fn [response]
                  (info response)
                  (handler
                   (-> response 
                       (get-in ["query" "pages"])
                       vals
                       first
                       (get "extract")
                       (clojure.string/replace #"\n" " - "))))))


  (def default-text "说到古代的巾帼英雄，大家可能首先想到的是穆桂英、花木兰之类的，但这些多数都是演义小说塑造出了的。当然，真实的历史上的巾帼英雄也不是没有，比如冼夫人，她可是周总理亲评的巾帼英雄第一人哦！")

(def mtones {"1" "first" "2" "second" "3" "third" "4" "fourth" "5" "neutral" nil ""})


(defn word-display
  [word characters]
  [:div.charcontainer
   (map (fn [char pron]
          [:div.char {:key char
                      :class (mtones (:tone pron))}
           [:span.metadata (:pinyin pron)]
           [:span.character.is-size-3 char]])
        word
        (or characters (repeat nil)))])

(defn token-div [i {word :text definition :definition :as token} show-level]
  "Creates a context-aware word-definition(s) div.
   `i` is a function (containing some `reagent/atom`s) that returns
   the number of definition-lines printed *before* the token, i.e.
   the height (in em) at which to print it.
   `show-level` is a `reagent/atom` of possible values [:none :first :all]
   `definition` is a collection of strings"
  [:div.token.is-inline-block {:key i :style {:height (str (+ 3 (* 1.5 (+ (i) (case @show-level :first 1 :all (count definition) :none 0)))) "em")}}
   [:div.word
    [:a
     {:class (when (empty? definition) " no-def")
      :on-click (fn [] (reset! show-level (if (<= (count definition) 1)
                                            (case @show-level
                                              :none :first
                                              :first :none)
                                            (case @show-level
                                              :none :first
                                              :first :all
                                              :all :none))))}
     [word-display word (:characters token)]]]
   [:div.definition.content
    {:style {:margin-top (str (* (i) 1.5) "em")}}
    (when (not= @show-level :none)
      (if (= @show-level :all)
        (do 
            (into [:ol]
                  (map (fn [main-def]
                         [:li.has-text-weight-semibold
                          (into [:ul]
                                (map (fn [li]
                                       [:li.is-italic.has-text-weight-normal (clojure.string/replace li #"/" " / ")])
                                     main-def))])
                       definition)))
        [:div
         (when (> (count definition) 1)
           [:a
            {:on-click #(reset! show-level :all)}
            "(...)"])
         [:p.is-italic (clojure.string/replace (str (ffirst definition)) #"/" " / ") ]]))]])

(defn submit-text [dictionary text callback]
  (words/get-words {:text text}
              (fn [word-map]
                (reset! dictionary word-map)
                (when callback (callback)))))

(defn partition-definition 
  "Split a long definition string in shorter (< 70 letters) strings
   without breaking words"
  ([definition]
   (partition-definition definition 70))
  ([definition n]
   (reduce (fn [acc current-def]
             (if (> (+ (count (peek acc))
                       (count current-def)) n)
               (conj acc current-def)
               (conj (pop acc) (str (peek acc) " " current-def))))
           [""]
           (clojure.string/split definition #"\s"))))

(defn special?
  ([char]
   (special? char :normal :open :close))
  ([char & types]
   (let [special {:normal (into #{} "")
                  :open (into #{} "（《(")
                  :close (into #{} ".,;、。，；）》)")}
         curr (apply clojure.set/union (vals (select-keys special types)))]
     (or
      (contains? curr char)
      (contains? curr (first char))))))

(defn partition-dictionary
  [n dict]
  (reverse
   (map reverse
        (reduce (fn [acc word]
                  (if (or (and (> (inc (count (first acc))) n)
                               (not (special? (:text word) :normal :close)))
                          (and (> (+ 2 (count (first acc))) n) ;; prevents ending line with opening punctuation
                               (special? (:text word) :open)))
                    (conj (conj (rest acc) (first acc) ) (list word))
                    (conj (rest acc) (conj (first acc) word)))) '() dict))))

(defn definition-row
  [dictionary]
  (into [:div.textcontainer]
        (loop [i 0
               tokens []
               showlevel-atoms []
               dict-list dictionary]
          (if (empty? dict-list)
            tokens
            (let [next-dictentry (first dict-list)
                  current-show-level (reagent/atom
                                      (if (< (:shown-level @state)
                                             (:level next-dictentry))
                                        :first
                                        :none))
                  count-previous-def-lines
                  #(apply + (map (fn [p-token p-showlevel]
                                   (let [n-defs (->> (nth p-token 2)
                                                     :definition
                                                     flatten
                                                     count)]
                                     (if (nil? n-defs)
                                       0
                                       (case @p-showlevel
                                         :none 0
                                         :first (min 1 n-defs)
                                         :all n-defs))))
                                 tokens
                                 showlevel-atoms))
                  next-token [token-div
                              count-previous-def-lines
                              (update-in next-dictentry
                                         [:definition]
                                         #(map partition-definition %)) ;; split long definitions
                              current-show-level]]
              (recur (inc i)
                     (conj tokens next-token)
                     (conj showlevel-atoms current-show-level)
                     (rest dict-list)))))))

(defn definition-rows-container
  [dictionary]
  (into [:div]
        (map definition-row (partition-dictionary 10 @dictionary))))

(defn contextual-definitions []
  [:div.section
   [kinese.components/navbar]
   [:div#contextual-definitions.container {:style {:overflow-x "scroll"}}
    [floating-menu]
    [definition-rows-container (reagent/cursor state [:dictionary])]]])

(defn textarea []
  (let [dictionary (reagent/cursor state [:dictionary])
        textarea? (reagent/cursor state [:text-controls :textarea?])
        loading? (reagent/cursor state [:text-controls :loading?])
        loading-random? (reagent/cursor state [:text-controls :loading-random?])]
    (fn []
      [:div#textarea-container
       [:div.field
        [:label.label.has-text-centered "Try me!"]
        [:div.control>textarea.textarea
         {:default-value default-text
          :rows 13}]]
       [:div#button-container.has-text-centered
        [:div.field>div.control>a.button.is-danger
         {:type "button"
          :class (when @loading? "is-loading is-success")
          :on-click (fn []
                      (swap! loading? not)
                      (submit-text dictionary
                                   default-text
                                   #(do (secretary/dispatch! "/contextual")
                                        (swap! loading? not))))}
         "Read this text!"]
        [:div.field>div.control
         [:label.label.has-text-centered "or"]
         [:a.button.is-primary
          {:type "button"
           :class (when @loading-random? " is-loading")
           :on-click (fn []
                       (reset! loading-random? true)
                       (get-random-text (fn [text]
                                          (submit-text
                                           (reagent/cursor state [:dictionary])
                                           text
                                           #(do (secretary/dispatch! "/contextual")
                                                (reset! loading-random? false))))))}
          "Read a random text"]]]])))
