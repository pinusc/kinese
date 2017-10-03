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
                      ;(when (< (count li) 10) 
                        ;(let [term (first n)
                        ;      tone (map #(vector (apply str (drop-last %)) (last %)) (string/split (first (fnext n)) #" "))
                        ;      definition (fnext (fnext n))]
                        ;  (println "n: " (string/split (first (fnext n)) #" "))
                        ;  (println "first n: " (first n))
                        ;  (println "len n" (count n))
                        ;  (println "")
                        ;  (println "")
                        ;  (println "############" )
                        ;  (println "---------TERM-----------" term)
                        ;  (println "------DEFINITION--------" definition)
                        ;  (println "---------TONE-----------" tone)
                        ;  (println "------TONE-TYPE---------" (type tone))
                        ;  (println "@@@@@@@@@@@@" )
                        ;  (println "")
                        ;  (println "")
                        ;  (println "N: " (conj (fnext (first li))))))
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
(get dict "山")

(get dict "你好")
(get dict "你")

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
                #_("https://opensource.keycdn.com/fontawesome/4.7.0/font-awesome.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(def MAXCOUNT 10)

(def default-text "孤山寺北贾亭西水面初平云脚低几处早莺争暖树谁家新燕啄春泥乱花渐欲迷人眼浅草才能没马蹄最爱湖东行不足绿杨阴里白沙堤")
(defn getwords [arg]
  (loop [i 0
         n 1
         res {}]
    (let [current-str (str (first (take n (drop i arg))))
          can-increment-n (and (< n MAXCOUNT) (< (dec (+ i n)) (count res)))
          can-increment-i (< i (count arg))]
      (do (print (first (take n (drop i arg))))
          (if (contains? dict current-str)
            (if can-increment-n
              (recur i (inc n) (assoc res current-str (get dict current-str)))
              (if can-increment-i
                (recur (inc i) 1 (assoc res current-str (get dict current-str)))
                (do (print "FOO") res)))
            (if can-increment-n 
              (recur i (inc n) res)
              (if can-increment-i
                (recur (inc i) 1 res)
                (do (print "BAR") res))))))))
(getwords default-text)
;EOF

(defn karacter [arg]
  ;; (println "kn" (count (map #(nth (first (get dict (str %))) 1) (:text arg))))
  (json-response {:karacters (into [] (map #(get dict (str %)) (:text arg)))
                  :words (into [] (getwords arg))}))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  (POST "/kar" [& _ :as {params :params} ] (karacter params))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
