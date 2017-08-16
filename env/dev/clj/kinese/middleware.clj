(ns kinese.middleware
  (:require [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions ]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :as ring-params]))

(use '[ring.middleware.transit :only [wrap-transit-params]]
     '[ring.util.response :only [response]])

(defn wrap-middleware [handler]
  (-> handler
      (wrap-transit-params {:encoding :json, :opts {}})
      (wrap-defaults api-defaults)
      wrap-exceptions
      wrap-reload
      ring-params/wrap-params))
