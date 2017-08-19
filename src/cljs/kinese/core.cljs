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
                             [:div.is-size-4 (map #(vector :p (clojure.string/capitalize %)) (clojure.string/split (nth definition 2 nil) #"/"))]])
                          string))
      [:div.def-buttons {:class (if (> (count string) 1) "is-visible" "is-hidden")}
       [:a.button.is-white {:on-click #(try (swap! current-def dec) (catch js/Object e nil))}
        [:span.icon [:i.fa.fa-arrow-left]]]
       [:a.button.is-white {:on-click #(try (swap! current-def inc) (catch js/Object e nil))}
        [:span.icon [:i.fa.fa-arrow-right]]]
       ]])
   ))

(defn kar [text tone definition]
  (when (and tone text definition)
    (let [current-def (reagent/atom 0)]
      (set-validator! current-def #(and (>= % 0) (< % (count tone))))
      [:div.kar
       {:on-mouse-over (fn [] (reset! definition [construct-definition text tone current-def]))}
       [:span {:class (nth gtones (dec (int (nth (first tone) 1))) "nil")} text]])))

(defn style [text tones definition]
  (into [:p] (map-indexed #(kar %2 (nth tones %1) definition) text)))

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
        [:input#submit-button.button {:type "button" 
                        :on-click (fn [] 
                                    (reset! submit? (not @submit?))
                                    (if @submit?
                                      (POST "/kar" {:params {:text @value}
                                                    :handler #(reset! curr (style @value % definition))})
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
