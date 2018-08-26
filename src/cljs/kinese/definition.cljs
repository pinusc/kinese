(ns kinese.definition)

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
  ([character definition current-def]
   (fn [character definition current-def]
     [:div.definition
      (if (string? definition)
        [:div
         [:h1.title.is-1.has-text-centered character]
         [:div.is-size-4 (for [s (clojure.string/split definition #"/")] 
                           ^{:key s}  [:p (clojure.string/capitalize s)] )]]
        (doall (map-indexed (fn [index definition] 
                              [:div {:key index :class (if (= @current-def index) "is-visible" "is-hidden")}
                               [:h1.title.is-1.has-text-centered character]
                               [:h2.subtitle.is-4.has-text-centered (reduce str (:pinyin definition) (:tone definition))]
                               [:div.is-size-4 (for [s (clojure.string/split (:definition definition) #"/")] 
                                                 ^{:key s}  [:p (clojure.string/capitalize s)] )]])
                            definition)))
      [:div.def-buttons {:class (if (> (count definition) 1) "is-visible" "is-hidden")}
       [:a.button.is-white {:on-click #(try (swap! current-def dec) (catch js/Object e nil))}
        "<"
        [:span.icon [:i.fa.fa-arrow-left]]]
       [:a.button.is-white {:on-click #(try (swap! current-def inc) (catch js/Object e nil))}
        ">"
        [:span.icon [:i.fa.fa-arrow-right]]]]])))
