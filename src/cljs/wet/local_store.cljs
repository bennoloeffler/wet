(ns wet.local-store
  (:require [cljs.reader :as r]))

(defn prjs
  "Print javascript objects to consloe"
  [obj]
  (.log js/console obj))

(defn ls-set-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (.setItem (.-localStorage js/window) key val))

(defn ls-set-clj-item!
  "Set `key' in browser's localStorage to clj data `val`."
  [key val]
  (ls-set-item! key (pr-str val)))

(defn ls-get-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (.getItem (.-localStorage js/window) key))

(defn ls-get-clj-item
  "Returns clj data value of `key' from browser's localStorage."
  [key]
  (r/read-string (ls-get-item key)))


(defn ls-remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))
