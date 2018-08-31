(ns kinese.components)

(defn navbar []
  [:nav.navbar.is-transparent.is-fixed-top
   [:div.navbar-brand
    [:a.navbar-item
     {:href "/"}
     [:h1.title.is-3 "Kinese"]]]
   [:div.navbar-menu
    [:div.navbar-end
     [:a.navbar-item
      {:href "/about"}
      "About"]]]])
