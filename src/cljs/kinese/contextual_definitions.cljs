(ns kinese.contextual-definitions
  (:require-macros [kinese.logging :refer [log info]])
  (:require [reagent.core :as reagent]
            [kinese.words :as words]))

  (def default-text "说到古代的巾帼英雄，大家可能首先想到的是穆桂英、花木兰之类的，但这些多数都是演义小说塑造出了的。当然，真实的历史上的巾帼英雄也不是没有，比如冼夫人，她可是周总理亲评的巾帼英雄第一人哦！")

(def mtones {"1" "first" "2" "second" "3" "third" "4" "fourth" "5" "neutral" nil ""})

(defonce state (reagent/atom {:text-controls {:textarea? false
                                              :loading? false}
                              :floating-menu {:open? false}
                              :shown-level 2
                              :dictionary '()}))

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

(defn submit-text [dictionary callback]
  (words/post {:text default-text}
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
  (into [:div.textcontainer.is-flex]
        (loop [i 0
               tokens []
               atoms []
               dict-list dictionary]
          (let [current-atom (reagent/atom (if (< (:shown-level @state)
                                                  (or (:level (first dict-list)) 7))
                                             :first
                                             :none))]
            (if (empty? dict-list)
              tokens
              (recur (inc i)
                     (conj tokens [token-div
                                   #(apply + (map (fn [tok ato]
                                                    (let [n-defs (->> (nth tok 2) :definition flatten count)]
                                                      (if (nil? n-defs)
                                                        0
                                                        (case @ato
                                                          :none 0
                                                          :first (min 1 n-defs)
                                                          :all n-defs))))
                                                  tokens
                                                  atoms)) ;; function to count used defininition lines
                                   (update-in (first dict-list)
                                              [:definition]
                                              #(map partition-definition %)) ;; split long definitions
                                   current-atom])
                     (conj atoms current-atom)
                     (rest dict-list)))))))

(defn definition-rows-container
  [dictionary]
  (into [:div]
        (map definition-row (partition-dictionary 10 @dictionary))))

(defn contextual-definitions []
  (let [dictionary (reagent/cursor state [:dictionary])
        textarea? (reagent/cursor state [:text-controls :textarea?])
        loading? (reagent/cursor state [:text-controls :loading?])]
    (fn []
      [:div.container
       (if @textarea?
         [:div
          [definition-rows-container dictionary]]
         [:div 
          [:div.field
           [:label.label "Insert text here"]
           [:div.control>textarea.textarea
            {:default-value default-text}]]
          [:div.control>a#submit-text.button.is-info.is-pulled-right
           {:type "button"
            :class (when @loading? "is-loading is-success")
            :on-click (fn []
                        (swap! loading? not)
                        (submit-text dictionary #(do (swap! loading? not)
                                                     (swap! textarea? not))))}
           "Submit text!"]])])))

(defn floating-menu
  []
  (let [fmenu (reagent/cursor state [:floating-menu])
        textarea? (reagent/cursor state [:text-controls :textarea?])
        shown-level (reagent/cursor state [:shown-level])
        open? (reagent/cursor fmenu [:open?])]
    (when (:textarea? (:text-controls @state))
      [:div#floating-menu.box
       [:a.is-pulled-right
        {:on-click #(swap! open? not)}
        [:span.icon.is-medium>i.fas.fa-2x
         {:class (if-not @open? " fa-bars has-text-info" " fa-times has-text-danger")}]]
       (when @open?
         [:div.content
          [:h3.is-title.is-5 "Menu"]
          [:div.field>input#change-text.button.is-link
           {:type "button"
            :value (str "Change text")
            :on-click #(swap! textarea? not)}]
          [:div.field
           [:label.label "Difficulty of words to show: "]
           [:input.slider.is-fullwidth
            {:step 1
             :min 0
             :max 6
             :default-value @shown-level
             :type "range"
             :on-change #(->> % .-target .-value int (reset! shown-level))}]
           [:p.has-text-centered.has-text-weight-semibold.has-text-info
            (cond
              (= 0 @shown-level) "All"
              (< 0 @shown-level 6) (str "HSK " (inc @shown-level) " or higher")
              (= @shown-level 6) "Not in HSK")]]])])))
