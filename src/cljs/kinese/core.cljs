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
   [:div
    [:h1.title.is-1 "Input your text to start!"]
    [:p.is-size-4 "The days of tens of tireless dictionary lookups just to understand a short piece of text are long gone! Input your text in the form on the right, and we will look up the characters and the words for you. Batteries included."]])
  ([character]
   [:div
    [:h1.title.is-1 character]
    [:p.is-size-4 "The definition for this character was not found. Sorry!"]])
  ([character string]
   [:div
    [:h1.title.is-1 character " (" (reduce str (take 2 string)) ")"]
    [:p.is-size-4 (nth string 2 nil)]]))

(defn kar [text tone definition]
  [:div.kar
    {:on-mouse-over (fn [] (reset! definition (construct-definition text tone)))}
    [:span {:class (nth gtones (dec (int (nth tone 1))) "nil")} text]])

(defn surround [text tone definition]
  (kar text tone definition))

(defn style [text tones definition]
  (into [:p] (map-indexed #(surround %2 (nth tones %1) definition) text)))

(defn get-style [curr value definition]
  (POST "/kar" {:params {:text value}
                :handler #(reset! curr (style value % definition))}))

(defn post-data [text]
  (POST "0.0.0.0:2000/kar" {:params {:text "foo"}
                            :format :json}))

(defn textarea [value styled curr content-editable?]
  [:div#textarea.textarea.is-size-3 {:content-editable (not @content-editable?)
                                     :suppressContentEditableWarning true
                                     :on-input #(reset! value (-> % .-target .-innerHTML))}
   @curr])


(defn text-input [definition]
  (let [value (reagent/atom default-text) 
        styled (reagent/atom [:p])
        curr (reagent/atom [:p "Insert your text here to start..."])
        submit? (atom true)]
    (fn []
      [:div
       [:div.control
        [textarea value styled curr submit?]]
       [:div.control
        [:input.button {:type "button" 
                        :on-click (fn [] 
                                    (reset! submit? (not @submit?))
                                    (if @submit?
                                      (get-style curr @value definition)
                                      (reset! curr @value)))
                        :value (if @submit? "Change text" "Submit")
                        :class (if @submit? "is-primary" "is-success")}]]])))

;; #(reset! value (-> % .-target .-value style))

(defn home-page []
  (let [definition (reagent/atom (construct-definition))]
    (fn []
      [:div
       [header]
       [:section.section 
        [:div.columns
         [:div.column
          @definition]
         [:div.column
          [text-input definition]]]]])))

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
