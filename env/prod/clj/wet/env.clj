(ns wet.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[wet started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[wet has shut down successfully]=-"))
   :middleware identity})
