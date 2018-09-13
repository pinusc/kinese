(ns kinese.components
  (:require [secretary.core :as secretary :include-macros true]))

(defn navbar []
  [:nav.navbar.is-transparent.is-fixed-top
   [:div.navbar-brand
    [:a.navbar-item
     {:on-click #(secretary/dispatch! "/")}
     [:img {:src "/logo.svg" :alt "Kinese: a smarter way to immerse yourself"}]]]
   [:div.navbar-menu
    [:div.navbar-end
     [:a.navbar-item
      {:href "/about"}
      "About"]]]])
