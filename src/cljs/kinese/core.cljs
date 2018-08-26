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
     [:h1.title.is-2.is-inline "Kinese, "]
     [:h2.subtitle.is-3.is-inline "a better approach to chinese learning"]]]])


(def gtones ["first" "second" "third" "fourth" "neutral"])

  (def default-text "米歇尔·阿弗拉克（1910年－1989年）是一位叙利亚哲學家、社会学家和阿拉伯民族主义者。他的理论对复兴社会主义的发展及其政治运动产生了深远影响；他被部分复兴社会主义者视为复兴社会主义学说的首要创始人。他生前出版了一些著作，主要有《为了复兴》、《唯一的归宿之战》和《反对扭曲阿拉伯革命运动的斗争》等。阿弗拉克出生于叙利亚大马士革的一个中產階級家庭。")

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

(defn character
  [text definition key]
   [:span 
    {:class (nth gtones (dec (int (:tone definition))) "nil")
     :key key} 
    text])

(defn special? [char]
  ;; (println char)
  (contains? (into #{} ".,;、（）。，《》；") char))

(defn word-div [text definition-div locked?]
  (when (and text definition-div)
    (let [current-def (reagent/atom 0)
          this-selected (reagent/atom false)]
      ;; (set-validator! current-def #(or (zero? (count definition)) (and (>= % 0) (< % (count definition)))))
      (fn [text definition-div locked?]
        (if (special? (:text text))
          [:span.kar
           (:text text)]
          [:span.word
           {:class (when @this-selected " selected")
            :on-mouse-over (fn [] 
                             (when-not @locked?
                               (reset! definition-div [construct-definition (:text text) (:definition text) current-def])))
            :on-click (fn [] 
                        (swap! locked? not)
                        (when @locked?
                          (reset! this-selected true))
                        (add-watch locked? :key #(do (remove-watch locked? %1) (when-not %4 (reset! this-selected false))))
                        (reset! definition-div  [construct-definition (:text text) (:definition text) current-def]))
            }
           (map-indexed #(character %2 (nth (:characters text) %1) %1) (:text text))])))))

(defn style-words
  "Creates <p> containing all words in `words`, styled accordingly to the
  pronunciation found in their definition "
  [words definition-div]
  (let [locked? (reagent/atom false)]
    (into [:div] (map (fn [w]
                      [word-div w definition-div locked?])
                    words))))

(defn textarea [raw-text textarea-value content-editable?]
  [:div#textarea.textarea.is-size-3 {:content-editable (not @content-editable?)
                                     :suppressContentEditableWarning true
                                     :on-input #(reset! raw-text (-> % .-target .-innerHTML))}
   @textarea-value])

(defn create-word-map
  [words dict]
  (map (fn [word]
         (let [entry (dict word)]
           {:definition (:definition (first entry))
            :characters (:pronunciation (first entry))
            :text word}))
       words))

(defn buttons [raw-text textarea-value submit? definition-div]
  "Handles the 'change/submit text' button."
  [:input#submit-button.button 
   {:type "button" 
    :on-click (fn [] 
                (reset! submit? (not @submit?))
                (if @submit?
                  (POST "/kar" {:params {:text @raw-text}
                                :response-format :json
                                :keywords? true
                                :handler (fn [response]
                                           (reset! textarea-value
                                                      (style-words (create-word-map (:segmented-text response) (reduce-kv #(assoc %1 (name %2) %3) {} (:words response))) definition-div)
                                                      ;; (style @raw-text (create-map (:karacters %)) definition-div)
                                                      ))})
                  (reset! textarea-value @raw-text)))
    :value (if @submit? "Change text" "Submit")
    :class (if @submit? "is-primary" "is-success")}])

(defn text-input [definition-div]
  (let [raw-text (reagent/atom default-text) 
        textarea-value (reagent/atom [:p "Insert your text here to start..."])
        submit? (atom true)]
    (fn []
      [:div
       [:div.control
        [textarea raw-text textarea-value submit?]]
       [:div.control
        [buttons raw-text textarea-value submit? definition-div]]])))

;; #(reset! value (-> % .-target .-value style))

(defn home-page []
  (let [definition-div (reagent/atom (construct-definition))]
    (fn []
      [:div
       [header]
       [:section.section 
        [:div.columns
         [:div.column.is-half
          @definition-div]
         [:div.column.is-half
          [text-input definition-div]]]]])))

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
