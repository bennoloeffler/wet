(ns wet.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [wet.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[wet started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[wet has shut down successfully]=-"))
   :middleware wrap-dev})
