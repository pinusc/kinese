(ns kinese.core
    (:require [reagent.core :as reagent :refer [atom]]
              [kinese.contextual-definitions :refer [contextual-definitions]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET]]
              [clojure.string :as string]))

(defn get-random-text
  []
  (println "foo")
  (get (GET "https://zh.wikipedia.org/w/api.php?action=query&format=json&origin=*&generator=random&prop=extracts&exlimit=1&exchars=200&exintro=true&explaintext=true")
        "query"))

(def default-tokens
  '(["foo" ["lorem ipsum dolor sit amet"]]
    ["bar" ["sed consecutura ali" "foobar lol"]]
    ["baz" ["merol muspi rolod tis tema"]]
    ["bat" ["sed consecutura ali" "foobar lol"]]
    ["bak" ["sed consecutura ali" "foobar lol"]]
    ))

(defn header []
  [:section.hero.is-primary 
   [:div.hero-body
    [:div.container
     [:h1.title.is-2.is-inline "Kinese, "]
     [:h2.subtitle.is-3.is-inline "a better approach to chinese learning"]]]])

(defn home-page []
  [:div
   [header]
   [:section.section 
    [contextual-definitions default-tokens]]])

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

