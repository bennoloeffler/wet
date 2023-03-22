(ns wet.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [wet.ajax :as ajax]
    [wet.events]
    [wet.timer :as timer]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href  uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "web timer"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click    #(swap! expanded? not)
                  :class       (when @expanded? :is-active)}
                 [:span] [:span] [:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "Home" :home]
                 [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:h1 "A litte Web Timer"]
   [:p "Written by Benno Löffler."]
   [:p "Done in clojure and clojurescript based on luminus"]])


(defn input-field
  "Assoc data-atom key value-of-input at every key stroke."
  [data-atom key type placeholder]
  [:input.input
   {:type      (name type) :placeholder placeholder
    :on-change #(let [val (-> % .-target .-value)]
                  (swap! data-atom assoc key val)
                  (println data-atom))}])

(defn home-page []
      #_[:section.section>div.container>div.content]
      [timer/timer-comp (* 60 120)])

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]

     ;[navbar]
     [page]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] #_(rf/dispatch-sync [:timer-init (* 60 20)]))}]}]
     ["/about" {:name :about
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
  (ajax/load-interceptors!)
  (mount-components)
  (rf/dispatch-sync [:timer-init (* 60 20)])
  (timer/bind-keys timer/key-fun-map))
