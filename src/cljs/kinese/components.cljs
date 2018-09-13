(ns kinese.components)

(defn navbar []
  [:nav.navbar.is-transparent.is-fixed-top
   [:div.navbar-brand
    [:a.navbar-item
     {:href "/"}
     [:img {:src "/logo.svg" :alt "Kinese: a smarter way to immerse yourself"}]]]
   [:div.navbar-menu
    [:div.navbar-end
     [:a.navbar-item
      {:href "/about"}
      "About"]]]])
