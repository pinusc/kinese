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
  ([character string current-def]
   (fn [character string current-def]
     [:div.definition
      (doall (map-indexed (fn [index definition] 
                            [:div {:key index :class (if (= @current-def index) "is-visible" "is-hidden")}
                             [:h1.title.is-1.has-text-centered character]
                             [:h2.subtitle.is-4.has-text-centered (reduce str (take 2 definition))]
                             [:div.is-size-4 (for [s (clojure.string/split (nth definition 2 nil) #"/")] 
                                               ^{:key s}  [:p (clojure.string/capitalize s)] )]])
                          string))
      [:div.def-buttons {:class (if (> (count string) 1) "is-visible" "is-hidden")}
       [:a.button.is-white {:on-click #(try (swap! current-def dec) (catch js/Object e nil))}
        [:span.icon [:i.fa.fa-arrow-left]]]
       [:a.button.is-white {:on-click #(try (swap! current-def inc) (catch js/Object e nil))}
        [:span.icon [:i.fa.fa-arrow-right]]]
       ]])
   ))

(defn kar [text tone definition locked?]
  (when (and tone text definition)
    (let [current-def (reagent/atom 0)
          this-selected (reagent/atom false)]
      (set-validator! current-def #(and (>= % 0) (< % (count tone))))
      (fn [text tone definition locked?]
        [:div.kar
         {:class (when @this-selected " selected")
          :on-mouse-over (fn [] 
                           (when-not @locked?
                             (reset! definition [construct-definition text tone current-def])))
          :on-click (fn [] 
                      (swap! locked? not)
                      (when @locked?
                        (reset! this-selected true))
                      (add-watch locked? :key #(do (remove-watch locked? %1) (when-not %4 (reset! this-selected false))))
                      (reset! definition [construct-definition text tone current-def])) }
         [:span 
          {:class (nth gtones (dec (int (nth (first tone) 1))) "nil")} 
          text]]))))

(defn style [text tones definition]
  (let [locked? (reagent/atom false)]
    (into [:p] (map-indexed (fn [tone text] 
                              [kar text (nth tones tone) definition locked?]) text))))

(defn textarea [raw-text curr content-editable?]
  [:div#textarea.textarea.is-size-3 {:content-editable (not @content-editable?)
                                     :suppressContentEditableWarning true
                                     :on-input #(reset! raw-text (-> % .-target .-innerHTML))}
   @curr])

(defn buttons [raw-text curr submit? definition-div]
  [:input#submit-button.button 
   {:type "button" 
    :on-click (fn [] 
                (reset! submit? (not @submit?))
                (if @submit?
                  (POST "/kar" {:params {:text @raw-text}
                                :handler #(reset! curr (style @raw-text % definition-div))})
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
