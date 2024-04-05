(ns wet.core
  (:require
    ;[day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    ;[goog.events :as events]
    ;[goog.history.EventType :as HistoryEventType]
    ;[markdown.core :refer [md->html]]
    ;[wet.ajax :as ajax]
    [wet.events]
    [wet.timer :as timer]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  #_(:import goog.History))

(defn home-page []
      #_[:section.section>div.container>div.content]
      [timer/timer-comp (* 60 120)])

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
     [page]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] #_(rf/dispatch-sync [:timer-init (* 60 20)]))}]}]
     #_["/about" {:name :about
                  :view #'about-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  ; just works (.initializeTouchEvents js/React true)
  (start-router!)
  ;(ajax/load-interceptors!)
  (mount-components)
  (rf/dispatch-sync [:timer-init (* 60 20)])
  (rf/dispatch-sync [:timer-sound])
  (timer/bind-keys timer/key-fun-map))
