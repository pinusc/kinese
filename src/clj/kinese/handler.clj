(ns kinese.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [kinese.middleware :refer [wrap-middleware]]
            [kinese.lang.chinese.dictionary :refer [read-dict]]
            [config.core :refer [env]]
            [clojure.string :as string]
            [ring.util.json-response :refer [json-response]]))

(def dict (read-dict "cedict_ts.u8"))
(def fnlp (org.fnlp.nlp.cn.CNFactory/getInstance "models"))

(def mount-target
  [:div#app
   [:div#no-js.modal.is-active
    [:div.modal-content
     [:h3 "You need to enable JavaScript in order to see this website!"]
     [:p "Either you disabled JavaScript or it hasn't loaded yet"]]]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css")
                "/css/bulma.css"
                "/css/bulma-slider.min.css"
                "https://use.fontawesome.com/releases/v5.3.1/css/all.css")])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn segment [text]
  (into [] (.seg fnlp text)))

(defn karacter [arg]
  ;; (println "kn" (count (map #(nth (first (get dict (str %))) 1) (:text arg))))
  (let [text (:text arg)
        segmented-text (segment text)]
    (json-response {:karacters (mapv #(get dict (str %)) text) 
                    :segmented-text (reduce (fn [li word]
                                              (if (dict word)
                                                (conj li word)
                                                (into li word)))
                                            [] segmented-text)
                    :words (reduce (fn [acc-dict word]
                                     (if-let [entry (dict word)]
                                       (assoc acc-dict word entry)
                                       (reduce #(assoc %1 %2 (get dict (str %2)))
                                               acc-dict word)))
                                   {} segmented-text)})))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  (POST "/kar" [& _ :as {params :params} ] (karacter params))
  (POST "/seg" [& _ :as {params :params} ] (segment params))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
