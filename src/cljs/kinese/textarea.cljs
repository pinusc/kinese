(ns kinese.textarea
  (:require [reagent.core :as reagent :refer [atom]]
            [kinese.definition]
            [kinese.words]))

(def gtones ["first" "second" "third" "fourth" "neutral"])

(defn character
  [text definition key]
  [:span 
   {:class (nth gtones (dec (int (:tone definition))) "nil")
    :key key} 
   text])

(defn special? [char]
  ;; (println char)
  (contains? (into #{} ".,;、（）。，《》；") char))

(defn word-div [text definition-div locked?]
  (when (and text definition-div)
    (let [current-def (reagent/atom 0)
          this-selected (reagent/atom false)]
      ;; (set-validator! current-def #(or (zero? (count definition)) (and (>= % 0) (< % (count definition)))))
      (fn [text definition-div locked?]
        (if (special? (:text text))
          [:span.kar
           (:text text)]
          [:span.word
           {:class (when @this-selected " selected")
            :on-mouse-over (fn [] 
                             (when-not @locked?
                               (reset! definition-div
                                       [kinese.definition/construct-definition
                                        (:text text)
                                        (:definition text) current-def])))
            :on-click (fn [] 
                        (swap! locked? not)
                        (when @locked?
                          (reset! this-selected true))
                        (add-watch locked? :key #(do (remove-watch locked? %1) (when-not %4 (reset! this-selected false))))
                        (reset! definition-div  [kinese.definition/construct-definition (:text text) (:definition text) current-def]))
            }
           (map-indexed #(character %2 (nth (:characters text) %1) %1) (:text text))])))))

(defn style-words
  "Creates <p> containing all words in `words`, styled accordingly to the
  pronunciation found in their definition "
  [words definition-div]
  (let [locked? (reagent/atom false)]
    (into [:div] (map (fn [w]
                        [word-div w definition-div locked?])
                      words))))

(defn textarea [raw-text textarea-value content-editable?]
  [:div#textarea.textarea.is-size-3 {:content-editable (not @content-editable?)
                                     :suppressContentEditableWarning true
                                     :on-input #(reset! raw-text (-> % .-target .-innerHTML))}
   @textarea-value])

(defn words-handler [textarea-value definition-div]
  "Returns the function to be passed to `kinese.words/post`.
   Do NOT pass this directly, as it accepts different arguments.
   This is necessary to allow the handler function to access `textarea-value` and `definition-div`"
  (fn [word-map]
    (reset! textarea-value
            (style-words word-map definition-div))))

(defn buttons [raw-text textarea-value submit? definition-div]
  "Handles the 'change/submit text' button."
  [:input#submit-button.button 
   {:type "button" 
    :on-click (fn [] 
                (reset! submit? (not @submit?))
                (if @submit?
                  (kinese.words/post {:text @raw-text}
                                     (words-handler textarea-value definition-div))
                  (reset! textarea-value @raw-text)))
    :value (if @submit? "Change text" "Submit")
    :class (if @submit? "is-primary" "is-success")}])

(defn text-input [definition-div]
  (let [raw-text (reagent/atom kinese.core/default-text) 
        textarea-value (reagent/atom [:p "Insert your text here to start..."])
        submit? (atom true)]
    (fn []
      [:div
       [:div.control
        [textarea raw-text textarea-value submit?]]
       [:div.control
        [buttons raw-text textarea-value submit? definition-div]]])))
