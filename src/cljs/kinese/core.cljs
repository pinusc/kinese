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
     [:h1.title "Welcome to kinese"]]]])


(def gtones ["first" "second" "third" "fourth" "neutral"])

(def default-text "孤山寺北贾亭西水面初平云脚低几处早莺争暖树谁家新燕啄春泥乱花渐欲迷人眼浅草才能没马蹄最爱湖东行不足绿杨阴里白沙堤")

(defn kar [text tone]
  [:div.kar
    {:on-click #(println "CLICK!")}
    [:span {:class (nth gtones (dec (int tone)) "nil")} text]])

(defn surround [text tone]
  (kar text tone))

(defn style [text tones]
  (println "te" (count text) text)
  (println "to" (count tones) tones)
    (into [:p] (map-indexed #(surround %2 (nth tones %1)) text)))

(defn get-style [curr value]
  (println (count value))
  (POST "/kar" {:params {:text value}
                :handler #(reset! curr (style value %))}))


(defn post-data [text]
  (POST "0.0.0.0:2000/kar" {:params {:text "foo"}
                            :format :json}))

(defn atom-input [value styled curr]
  [:div.textarea.is-size-3 {:content-editable true
                            :suppressContentEditableWarning true
           :on-input #(reset! value (-> % .-target .-innerHTML)) 
           :on-blur #(get-style curr @value) 
           :on-focus #(reset! curr @value)}
   @curr])

;; #(reset! value (-> % .-target .-value style))

(defn home-page []
  (let [val (reagent/atom default-text) 
        styled (reagent/atom [:p])
        curr (reagent/atom [:p])]
    (fn []
      [:div
       [header]
       [:section.section 
        [:div.container
         [:h4.title.is-4 "Definition:"]]
        [:div.container
         [:div.field
          [:div.control
           [atom-input val styled curr]]
          [:button.button {:type "submit"} "Submit"]]]]])))

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
