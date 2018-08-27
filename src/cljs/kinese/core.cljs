(ns kinese.core
    (:require [reagent.core :as reagent :refer [atom]]
              [kinese.textarea]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [clojure.string :as string]))

(def default-text "米歇尔·阿弗拉克（1910年－1989年）是一位叙利亚哲學家、社会学家和阿拉伯民族主义者。他的理论对复兴社会主义的发展及其政治运动产生了深远影响；他被部分复兴社会主义者视为复兴社会主义学说的首要创始人。他生前出版了一些著作，主要有《为了复兴》、《唯一的归宿之战》和《反对扭曲阿拉伯革命运动的斗争》等。阿弗拉克出生于叙利亚大马士革的一个中產階級家庭。")

(defn header []
  [:section.hero.is-primary 
   [:div.hero-body
    [:div.container
     [:h1.title.is-2.is-inline "Kinese, "]
     [:h2.subtitle.is-3.is-inline "a better approach to chinese learning"]]]])

(defn home-page []
  (let [definition-div (reagent/atom (kinese.definition/construct-definition))]
    (fn []
      [:div
       [header]
       [:section.section 
        [:div.columns
         [:div.column.is-half
          @definition-div]
         [:div.column.is-half
          [kinese.textarea/text-input definition-div]]]]])))

(defn about-page []
  [:div [:h2 "About kinese"]
   [:div [:a {:href "/"} "go to the home page"]]])

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

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

