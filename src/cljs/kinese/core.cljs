(ns kinese.core
    (:require [reagent.core :as reagent :refer [atom]]
              [kinese.contextual-definitions :refer [contextual-definitions textarea]]
              [kinese.data :refer [state]]
              [kinese.components]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET]]
              [clojure.string :as string]))

(def page (reagent/cursor state [:page]))

(defn home-page []
  [:div
   [:section.hero.is-info.is-bold.is-overlay
    [kinese.components/navbar]
    [:div.hero-body>div.container
     [:div.columns
      [:div#title-column.column.is-half
       [:h1.title.is-2.is-inline "Kinese, "]
       [:h2.subtitle.is-3.is-inline "a better approach to language learning"]]
      [:div.column.is-half
       [textarea]]]]]])

(defn about-page []
  [:div
   [:section.hero.is-info.is-medium
    [kinese.components/navbar]
    [:div.hero-body
     [:h1.title.is-1 "About kinese"]]]
   [:section.section
    [:h1.title.is-1 "About kinese"]]])

(defn current-page []
  [:div
   [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

(secretary/defroute "/contextual" []
  (reset! page #'contextual-definitions))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (reset! page #'home-page)
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
