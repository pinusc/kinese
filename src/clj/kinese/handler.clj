(ns kinese.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [kinese.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [clojure.string :as string]
            [ring.util.json-response :refer [json-response]]))

(defn read-dict [path]
  (let [txt (slurp path)]
    (into {}
          (reduce (fn [li n] 
                    (do 
                        (let [term (first n)
                              tone (map #(vector (apply str (drop-last %)) (last %)) (string/split (first (fnext n)) #" "))
                              definition (fnext (fnext n))]
                          (if (= term (ffirst li))
                            (conj (next li) [term (conj (fnext (first li)) [tone definition])])
                            ;(conj (next li) [(first n) (next (first li))])
                            (conj li [term [[tone definition]]])))))
                  '()
                  (map
                    #(let [t (next %)]
                       [(first t) (next t)]) ;( vector (ffirst %2) (nnext %2))) 
                    (re-seq #"(?m)^.* (.*) \[((?:\w+\d\s?)+)\] \/(.*)\/$" txt))))))
;(re-seq #".* (.*) \[(\w+)(\d)\] \/(.*)\/\n" txt)))

(def dict (read-dict "cedict_ts.u8"))
(def fnlp (org.fnlp.nlp.cn.CNFactory/getInstance "models"))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css")
                "/css/bulma.css"
                #_("https://opensource.keycdn.com/fontawesome/4.7.0/font-awesome.min.css"))])

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
    (json-response {:karacters (into [] (map #(get dict (str %)) text)) 
                    :segmented-text segmented-text
                    :words (reduce #(assoc %1 %2 (get dict %2)) {} segmented-text)})))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  (POST "/kar" [& _ :as {params :params} ] (karacter params))
  (POST "/seg" [& _ :as {params :params} ] (segment params))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
