(ns kinese.contextual-definitions
  (:require-macros [kinese.logging :refer [log info]])
  (:require [reagent.core :as reagent]
            [kinese.words :as words]))

(def default-text "是一位叙利亚哲學家、社会学家和阿拉伯民族主义者。他的理论对复兴社会主义的发展及其政治运动产生了深远影响；他被部分复兴社会主义者视为复兴社会主义学说的首要创始人。他生前出版了一些著作，主要有《为了复兴》、《唯一的归宿之战》和《反对扭曲阿拉伯革命运动的斗争》等。阿弗拉克出生于叙利亚大马士革的一个中產階級家庭。")

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
  [:div.token {:key i :style {:height (str (+ 3 (* 1.5 (+ (i) (case @show-level :first 1 :all (count definition) :none 0)))) "em")}}
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
   [:div.definition
    {:style {:margin-top (str (* (i) 1.5) "em")}}
    [:em 
     (when (not= @show-level :none)
       (if (= @show-level :all)
         (into [:ul] (map (fn [i] [:li i]) definition))
         (first definition)))]]])

(defn submit-text [dictionary]
  (words/post {:text default-text}
              (fn [word-map]
                ;; (info word-map)
                (reset! dictionary word-map))))

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

(info (special? "."))

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
  (into [:div.textcontainer.is-flex]
        (loop [i 0
               tokens []
               atoms []
               dict-list dictionary]
          (let [current-atom (reagent/atom :first)]
            (info )
            (if (empty? dict-list)
              tokens
              (recur (inc i)
                     (conj tokens [token-div
                                   #(apply + (map (fn [tok ato]
                                                    (let [n-defs (->> (nth tok 2) :definition count)]
                                                      (if (nil? n-defs)
                                                        0
                                                        (case @ato
                                                          :none 0
                                                          :first (min 1 n-defs)
                                                          :all n-defs))))
                                                  tokens
                                                  atoms)) ;; function to count used defininition lines
                                   (update-in (first dict-list) [:definition] #(flatten (map partition-definition %))) ;; split long definitions
                                   current-atom])
                     (conj atoms current-atom)
                     (rest dict-list)))))))

(defn definition-rows-container
  [dictionary]
  (info @dictionary)
  (into [:div]
        (map definition-row (partition-dictionary 10 @dictionary))))

(defn contextual-definitions []
  (let [dictionary (reagent/atom '())]
    (submit-text dictionary)
    (fn []
      [:div.container 
       [:input.button
        {:type "button"
         :value "Submit random text!"
         :on-click #(submit-text dictionary)}]
       [definition-rows-container dictionary]])))
