(ns kinese.floating-menu
  (:require [reagent.core :as reagent]
            [kinese.data :refer [state]]))

(defn difficulty-slider
  [shown-level]
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
      (= @shown-level 6) "Not in HSK")]])

(defn floating-menu
  []
  (let [textarea? (reagent/cursor state [:text-controls :textarea?])
        shown-level (reagent/cursor state [:shown-level])
        open? (reagent/cursor state [:floating-menu :open?])]
    (when (:textarea? (:text-controls @state))
      [:div#floating-menu.box
       [:a.is-pulled-right
        {:on-click #(swap! open? not)}
        [:span.icon.is-medium>i.fas.fa-2x
         {:class (if-not @open? " fa-bars has-text-info" " fa-times has-text-danger")}]]
       (when @open?
         [:div.content
          [:h3.is-title.is-5 "Menu"]
          [:div.field>input.button.is-link
           {:type "button"
            :value (str "Change text")
            :on-click #(swap! textarea? not)}]
          [difficulty-slider shown-level]])])))
