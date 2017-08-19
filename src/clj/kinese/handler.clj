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
                      (if (= (first n) (first  (first li)))
                                 (conj (next li) [(first n) (conj (first (next (first li))) (first (next n)))])
                                 ;(conj (next li) [(first n) (next (first li))])
                                 (conj li [(first n) (next n)])))
                    '()
                    (map
                      #(let [t (next %)]
                         [(first t) (next t)]) ;( vector (ffirst %2) (nnext %2))) 
                      (re-seq #"(?m)^.* (.*) \[(\w+)(\d)\] \/(.*)\/$" txt))))))
        ;(re-seq #".* (.*) \[(\w+)(\d)\] \/(.*)\/\n" txt)))

(def dict (read-dict "cedict_ts.u8"))

(ffirst (first (get dict "过")))

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
                "https://opensource.keycdn.com/fontawesome/4.7.0/font-awesome.min.css")])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn karacter [arg]
  (println "kn" (count (map #(nth (first (get dict (str %))) 1) (:text arg))))
  (json-response (into [] (map #(get dict (str %)) (:text arg)))))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  (POST "/kar" [& _ :as {params :params} ] (karacter params))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
