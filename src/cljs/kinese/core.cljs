(ns kinese.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [clojure.string :as string]
              [ajax.core :refer [POST]]))

;; -------------------------
;; Views

(defn header []
  [:section.hero.is-primary 
   [:div.hero-body
    [:div.container
     [:h1.title.is-2.is-inline "Kinese, "]
     [:h2.subtitle.is-3.is-inline "a better approach to chinese learning"]]]])


(def gtones ["first" "second" "third" "fourth" "neutral"])

(def default-text "孤山寺北贾亭西水面初平云脚低几处早莺争暖树谁家新燕啄春泥乱花渐欲迷人眼浅草才能没马蹄最爱湖东行不足绿杨阴里白沙堤")

(defn construct-definition
  ([]
   (fn []
     [:div
      [:h1.title.is-1 "Input your text to start!"]
      [:p.is-size-4 "The days of tens of tireless dictionary lookups just to understand a short piece of text are long gone! Input your text in the form on the right, and we will look up the characters and the words for you. Batteries included."]]))
  ([character]
   (fn [character]
     [:div
      [:h1.title.is-1 character]
      [:p.is-size-4 "The definition for this character was not found. Sorry!"]]))
  ([character definition current-def]
   (fn [character definition current-def]
     [:div.definition
      (doall (map-indexed (fn [index definition] 
                            [:div {:key index :class (if (= @current-def index) "is-visible" "is-hidden")}
                             [:h1.title.is-1.has-text-centered character]
                             [:h2.subtitle.is-4.has-text-centered (reduce str (:pinyin definition) (:tone definition))]
                             [:div.is-size-4 (for [s (clojure.string/split (:definition definition) #"/")] 
                                               ^{:key s}  [:p (clojure.string/capitalize s)] )]])
                          definition))
      [:div.def-buttons {:class (if (> (count definition) 1) "is-visible" "is-hidden")}
       [:a.button.is-white {:on-click #(try (swap! current-def dec) (catch js/Object e nil))}
        "<"
        [:span.icon [:i.fa.fa-arrow-left]]]
       [:a.button.is-white {:on-click #(try (swap! current-def inc) (catch js/Object e nil))}
        ">"
        [:span.icon [:i.fa.fa-arrow-right]]]]])))

(defn kar [text definition definition-div locked?]
  (when (and text definition definition-div)
    (let [current-def (reagent/atom 0)
          this-selected (reagent/atom false)]
      (set-validator! current-def #(or (zero? (count definition)) (and (>= % 0) (< % (count definition)))))
      (fn [text definition definition-div locked?]
        [:div.kar
         {:class (when @this-selected " selected")
          :on-mouse-over (fn [] 
                           (when-not @locked?
                             (reset! definition-div [construct-definition text definition current-def])))
          :on-click (fn [] 
                      (swap! locked? not)
                      (when @locked?
                        (reset! this-selected true))
                      (add-watch locked? :key #(do (remove-watch locked? %1) (when-not %4 (reset! this-selected false))))
                      (reset! definition-div [construct-definition text definition current-def]))}
         [:span 
          {:class (nth gtones (dec (int (:tone (first definition)))) "nil")} 
          text]]))))

(defn style [text definitions definition-div]
  (let [locked? (reagent/atom false)]
    (into [:p] (map-indexed (fn [i text] 
                              [kar text (nth definitions i) definition-div locked?]) text))))

(defn textarea [raw-text curr content-editable?]
  [:div#textarea.textarea.is-size-3 {:content-editable (not @content-editable?)
                                     :suppressContentEditableWarning true
                                     :on-input #(reset! raw-text (-> % .-target .-innerHTML))}
   @curr])

(defn create-map
  "Glue function. Flattens a list of maps as the JSON returned by the /kar
  endpoint so that they are in the form {:definition ... :tone ... :pinyin ...},
  accepted by the `style` function"
  [raw-text]
  (for [line raw-text]
    (for [{[{tone :tone pinyin :pinyin} & other ] :pronunciation definition :definition} line]
      {:definition definition
       :tone tone
       :pinyin pinyin})))

(defn buttons [raw-text curr submit? definition-div]
  "Handles the 'change/submit text' button."
  [:input#submit-button.button 
   {:type "button" 
    :on-click (fn [] 
                (reset! submit? (not @submit?))
                (if @submit?
                  (POST "/kar" {:params {:text @raw-text}
                                :response-format :json
                                :keywords? true
                                :handler #(do (reset! curr (style @raw-text (create-map (:karacters %)) definition-div)))})
                  (reset! curr @raw-text)))
    :value (if @submit? "Change text" "Submit")
    :class (if @submit? "is-primary" "is-success")}])


(defn text-input [definition-div]
  (let [raw-text (reagent/atom default-text) 
        curr (reagent/atom [:p "Insert your text here to start..."])
        submit? (atom true)]
    (fn []
      [:div
       [:div.control
        [textarea raw-text curr submit?]]
       [:div.control
        [buttons raw-text curr submit? definition-div]]])))

;; #(reset! value (-> % .-target .-value style))

(defn home-page []
  (let [definition-div (reagent/atom (construct-definition))]
    (fn []
      [:div
       [header]
       [:section.section 
        [:div.columns
         [:div.column
          @definition-div]
         [:div.column
          [text-input definition-div]]]]])))

(defn about-page []
  [:div [:h2 "About kinese"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
