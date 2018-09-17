(ns kinese.components
  (:require [secretary.core :as secretary :include-macros true]))

(defn navbar []
  [:nav.navbar.is-transparent.is-fixed-top
   [:div.navbar-brand
    [:a.navbar-item
     {:on-click #(secretary/dispatch! "/")}
     [:h3.title.is-3 "Kinese"]]]
   [:div.navbar-menu
    [:div.navbar-end
     [:a.navbar-item
      {:href "/about"}
      "About"]]]])
