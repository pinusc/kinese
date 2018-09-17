(ns kinese.core
    (:require [reagent.core :as reagent :refer [atom]]
              [kinese.contextual-definitions :refer [contextual-definitions textarea]]
              [kinese.data :refer [state]]
              [kinese.components]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET]]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [clojure.string :as string])
    (:import goog.History))

(def page (reagent/cursor state [:page]))

(defn home-page []
  [:div
   [:section.hero.is-info.is-bold
    [kinese.components/navbar]
    [:div.hero-body>div.container
     [:div.columns
      [:div#title-column.column.is-half
       [:h1.title.is-2.is-inline "Kinese, "]
       [:h2.subtitle.is-3.is-inline "a better approach to language learning"]
       [:h4.subtitle.is-5 "Kinese provides an interactive interlinear dictionary for any text you insert. It gets out of your way and lets you read faster and with less effort!"]]
      [:div.column.is-half
       [textarea]]]]]])

(defn about-page []
  [:div
   [:section.hero.is-info.is-medium
    [kinese.components/navbar]
    [:div.hero-body
     [:h1.title.is-1 "About kinese"]]]
   [:section.section>div.container
    [:h3.title.is-3 "Free, open source"]
    [:p
     "The project is released under the "
     [:a {:href "https://www.gnu.org/licenses/#GPL"} "Gnu General License (v3)."]]

    [:p
     "Kinese was written in Clojure + Ring on the backend, and ClojureScript + Reagent on the frontend. The code "
     [:a {:href "https://github.com/pinusc/kinese"} "is available on Github"]]
    
    ]
   [:section.section>div.container
    [:h3.title.is-3 "Data sources"]
    [:p "All the dictionaries used to deliver the content are freely licensed (under Createive commons or GPL licenses). In order to support as many languages as possible, we use data from a variety of sources."]
    [:p "Many thanks to: "]
    [:ul#sources-list
     [:li [:a {:href "https://www.mdbg.net/chinese/dictionary?page=cedict"} "CC-CEDICT"]]
     [:li [:a {:href "https://freedict.org"} "FreeDict"]]]]
   [:section.section>div.container
    [:h3.title.is-3 "Acknowledgements and inspiration"]
    [:p "This project was heavily inspired by "
     [:a {:href "http://nodictionaries.com"} "NoDictionaries"]]
    ]])



(defn current-page []
  [:div
   [:div#not-landscape.modal.has-background-warning.is-active
    [:div.modal-content
     [:h2.title.is-2 "This page looks best in landscape mode!"]
     [:div.level>div.level-item
      [:figure.image.is-128x128
       [:img {:src "/rotate_to_landscape.svg"}]]]]]
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

;; Quick and dirty history configuration.
(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))
