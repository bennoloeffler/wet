(ns wet.timer
  (:require
    [wet.local-store :as ls]
    [goog.events.KeyCodes :as keycodes]
    [goog.events :as gev]
    [goog.object :as gobj]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [wet.validation :refer [percentage]]
    [clojure.string :as str]
    #_[re-com.core :refer [at h-box v-box box gap line label title slider checkbox input-text horizontal-bar-tabs vertical-bar-tabs p]])
  (:import [goog.events EventType KeyHandler]))



;;
;; <sub >evt
;; https://lambdaisland.com/blog/2017-02-11-re-frame-form-1-subscriptions
;;
;; :<- combine subs
;; https://day8.github.io/re-frame/subscriptions/


;; CONCEPT of the timer
;;
;; show the following:
;; A circle with the time left in "percent of the circle"
;; In the circle, a big number with left minutes or seconds, if below 60 sec.
;; A horizontal slider to set the timer.
;; Time as wallclock:
;;  - starting of timer,
;;  - current wallclock
;;  - and expected end in wallclock-time
;; a checkbox for beep
;; An visual alarm or beep at the end of the timer
;;
;; interactions:
;;
;; - have a preset duration: 20 min.
;; - have modes: stopped, running
;; - have slider set visually the minutes in small timer-ui
;; - in stopped mode
;;     - have buttons: start and resume (resume only after first start)
;; - in running mode
;;     - have one buttons: stop
;; - button start: starts with duration set by the user
;; - button resume: resumes after stopped without restarting full duration
;; - button stop: stops decreasing time
;;
;;
;; DATA in db:
;;
;; timer-duration-secs - the start duration of the timer
;; timer-time - js/Date current time, wallclock
;; timer-start - js/Date when was the timer started, wallclock
;; timer-end - js/Date when is the timer to be
;;             finished (including pauses), wallclock
;; timer-remaining-secs - secs that are remaining.
;; timer-state - :started :stopped
;; timer-first-start - before the first pressing of button start.
;;                     In order to avoid resume button.
;;



;;
;; js helper
;;
(defn js-obj->clj-map
  "Uses the Google Closure object module to get the keys and values of any JavaScript Object
  and put them into a ClojureScript map"
  [obj]
  (zipmap (gobj/getKeys obj) (gobj/getValues obj)))

;;
;; keyboard
;;
;; https://github.com/reagent-project/historian/blob/master/src/cljs/historian/keys.cljs

(def key-fun-map
  {keycodes/LEFT  #(rf/dispatch [:set-timer-plus (* 60 -1)])
   keycodes/RIGHT #(rf/dispatch [:set-timer-plus (* 60 1)])
   keycodes/UP    #(rf/dispatch [:set-timer-plus 1])
   keycodes/DOWN  #(rf/dispatch [:set-timer-plus -1])
   keycodes/SPACE #(rf/dispatch [:timer-toggle])
   keycodes/ENTER #(rf/dispatch [:timer-start])})

(defn bind-keys
  "Bind KEYDOWN.
   Maps to funs of the key-fun-map.
   Example key-fun-map:
   {keycodes/LEFT #(rf/dispatch [:move-left])
    keycodes/RIGHT #(println \"right\")
    keycodes/SPACE #(println %)}"
  [key-fun-map]
  (gev/listen js/window EventType.KEYDOWN
                 #(do
                    (.preventDefault %)
                    (let [code (.-keyCode %)
                          fun (key-fun-map code)] ;; 90 is Z
                      ;(println "key-code: " code)
                      ;(println "event:")
                      ;(println (js-obj->clj-map %))
                      (when fun (fun))))))
;;
;; date time seconds helper
;;
(defn subtract-in-secs
  [date-later date-earlier]
  (-> (.getTime date-later)
      (- (.getTime date-earlier))
      (/ 1000)
      (long)))

(defn add-seconds
  ([s] (add-seconds (js/Date.) s))
  ([d s] (js/Date. (+ (.getTime d) (* 1000 s)))))

;;
;; re-frame helper
;;
(def <watch (comp deref rf/subscribe))
(def sub rf/subscribe)
(def evt> rf/dispatch)

;;
;; timer: call every second
;;
(defn dispatch-timer-event
  []
  (let [now (js/Date.)]
    (evt> [:timer-time now])))

;; call the dispatching function every second
(defonce do-timer (js/setInterval dispatch-timer-event 1000))

;;
;; get mouse data
;;
(defn get-mouse-x-y [event #_size]
  (let [target (.-currentTarget event) ; not .-target otherwise: Children!
        rect (.getBoundingClientRect target)
        touches (.-touches event)
        event (if touches
                (aget touches 0)
                event)
        tx (.-left rect)
        ty (.-top rect)
        xm (.-clientX event)
        ym (.-clientY event)
        x (* (- xm tx)  1 #_zx)
        y (* (- ym ty)  1 #_zy)]
    {:x x :y y :height  (.-height rect) :width (.-width rect)}))

;;
;; the visual UI only (without buttons and config components)
;;
(defn timer
  "size: how big should it be in pixel
   timer-duration-ticks: what is the biggest number to show
   timer-remaining-ticks: what is the current remaining number to show in UI"
  [size timer-remaining-ticks timer-duration-ticks status]
  (let [dragging (atom false)
        start-drag (fn [event]
                     (.preventDefault event)
                     (reset! dragging true))
        drag (fn [event]
               (.preventDefault event)
               (when @dragging
                 (let [data (get-mouse-x-y event)]
                   (rf/dispatch [:set-timer-percent
                                 (percentage (:width data)
                                             (:height data)
                                             (:x data)
                                             (:y data))]))))
        stop-drag (fn [event]
                    (.preventDefault event)
                    (reset! dragging false))]

   (fn []
      [:div.timer-svg
        (when (not= @status :running)
         {:on-mouse-down (fn [event] (start-drag event))
          :on-touch-start (fn [event] #_(evt> [:user-event "touch start"])(start-drag event))

          :on-mouse-move (fn [event] (drag event))
          :on-touch-move (fn [event] #_(evt> [:user-event "touch move"])(drag event))

          :on-mouse-up (fn [event] (stop-drag event))
          :on-mouse-leave (fn [event] (stop-drag event))
          :on-touch-end (fn [event] #_(evt> [:user-event "touch end"])(stop-drag event))
          ;:on-touch-leave (fn [event] (stop-drag event)) unknown?
          :on-touch-cancel (fn [event] #_(evt> [:user-event "touch cancel"])(stop-drag event))

          :on-click (fn [event]
                      ;(.preventDefault event)
                      (let [target (.-currentTarget event) ; not .-target otherwise: Children!
                            data (get-mouse-x-y event #_size)]
                        (rf/dispatch [:set-timer-percent
                                      (percentage (:width data)
                                                  (:height data)
                                                  (:x data)
                                                  (:y data))])))})
       (let [;; pixel ticks on the circle - in order to make it more lines
             scaled-duration-ticks          360
             scaled-remaining-ticks         (* @timer-remaining-ticks (/ scaled-duration-ticks @timer-duration-ticks))
             half                           (/ size 2)
             circle-fn                      (fn [t] (let [rad (-> t
                                                                  (* 2 Math/PI)
                                                                  (/ scaled-duration-ticks)
                                                                  (+ Math/PI)) ; start top
                                                          x1  (- (Math/sin rad)) ;  - clockwise
                                                          y1  (Math/cos rad)]
                                                      [(+ half (* x1 half))
                                                       (+ half (* y1 half))
                                                       t]))
             all-ticks                      (range scaled-remaining-ticks)
             all-pos                        (map circle-fn all-ticks)
             zoom500                        (/ size 500)
             scale-mins                     (* zoom500 ;; baseline text fits to size 500
                                               (condp >= @timer-remaining-ticks
                                                 -10000 3
                                                 -1000 3
                                                 -100 6
                                                 -10 8
                                                 -1 10
                                                 9 13
                                                 99 9
                                                 999 7
                                                 9999 5
                                                 3))
             scale-translate-minutes        (str "translate(" half "," half ") scale(" scale-mins ")")
             scale-translate-duration       (str "translate(" half "," (- half (* 128 zoom500)) ")scale(" (* zoom500 2.8) ")")
             scale-translate-remaining-secs (str "translate(" half " " (+ half (* 80 zoom500)) ") scale(" (* zoom500 1.5) ")")
             remaining-secs                 (mod @timer-remaining-ticks 60)]
         [:svg {:background    "white"
                :viewBox       "0 0 400 400"
                :width         "100vw"
                :height        "70vh"}
          (doall (for [[x y k] all-pos]
                   ^{:key k} [:line {:x1 half :y1 half :x2 x :y2 y :stroke
                                     (if (= @status :running) "red" "lightgrey") :stroke-width 1}]))

          [:circle {:r    (/ half 2), :cx half, :cy half,
                    :fill :white :stroke "red" :stroke-width (/ half 6)}]
          (when (>= (abs @timer-remaining-ticks) 60)
            [:text {:x                  0 :y 0
                    :text-anchor        "middle"
                    :alignment-baseline "central"
                    :transform          scale-translate-remaining-secs :fill "red" :stroke "red"}
             (if (< @timer-remaining-ticks 0)
               (str (- 60 remaining-secs))
               (str remaining-secs))])
          [:text {:x                  0 :y 0
                  :text-anchor        "middle"
                  :alignment-baseline "central"
                  :transform          scale-translate-minutes :fill "red" :stroke "red"} (str (if (< (abs @timer-remaining-ticks) 60) @timer-remaining-ticks (quot @timer-remaining-ticks 60)))]
          [:text {:x                  0 :y 0
                  :text-anchor        "middle"
                  :alignment-baseline "central"
                  :transform          scale-translate-duration :fill "white"} (str (quot @timer-duration-ticks 60))]])])))


(defn get-client-rect [node]
  (let [r (.getBoundingClientRect node)]
    {:left   (.-left r)
     :top    (.-top r)
     :right  (.-right r)
     :bottom (.-bottom r)
     :width  (.-width r)
     :height (.-height r)}))

;; https://stackoverflow.com/questions/39831137/force-reagent-component-to-update-on-window-resize)


;; mobile audio forces to play on user interaction. Bäääh...
(def alarm (atom nil))

(defn stop-alarm
  []
  (.load @alarm)
  nil)

(defn play-alarm
  []
  (.play @alarm)
  nil)

(defn init-audio
  "start once on an interaction in order to replay afterwards on mobile browsers"
  []
  (when-not @alarm
    (reset! alarm (js/Audio. "deep-meditation-bell.mp3" #_"Softchime.mp3"))
    ;(set! (.. @alarm -loop) true)
    (play-alarm)
    (js/setTimeout #(stop-alarm) 10)))

;;
;; ALL buttons
;;

(defn start-button [durationDisplay]
  [:a.button.is-primary.is-light.mr-1.mt-1
   {:on-click #(do (init-audio)
                   (rf/dispatch [:timer-start]))}
   [:span (str "start: " durationDisplay)]])

(defn re-start-button [durationDisplay]
  [:a.button.is-primary.is-light.mr-1.mt-1
   {:on-click #(do (stop-alarm)
                   (rf/dispatch [:timer-re-start
                                 (if (str/ends-with? durationDisplay "s")
                                   (js/parseInt (str/replace durationDisplay #"s" ""))
                                   (* 60 (js/parseInt durationDisplay)))]))}
   [:span (str "re-start: " durationDisplay)]])

(defn stop-button []
  [:a.button.is-danger.is-light.mr-1.mt-1
   {:on-click #(do (stop-alarm)
                   (rf/dispatch [:timer-stop]))}
   [:span "stop"]])

(defn resume-button [remainingDisplay]
  [:a.button.is-primary.is-light.mr-1.mt-1
   {:on-click #(do (init-audio)
                   (rf/dispatch [:timer-resume]))}
   [:span (str "resume: " remainingDisplay)]])

(defn m1-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-duration-and-remaining-secs (* 60 1)])}
   [:span "1"]])

(defn m5-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-duration-and-remaining-secs (* 60 5)])}
   [:span "5"]])

(defn m10-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-duration-and-remaining-secs (* 60 10)])}
   [:span "10"]])

(defn m20-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-duration-and-remaining-secs (* 60 25)])}
   [:span "25"]])

(defn m+5-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-plus (* 60 5)])}
   [:span "+5"]])

(defn m-5-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-plus (* -60 5)])}
   [:span "-5"]])

(defn m-1-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-plus (* -60 1)])}
   [:span "-1"]])

(defn m+1-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-plus (* 60 1)])}
   [:span "+1"]])

(defn m-1s-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-plus -1])}
   [:span "-1s"]])

(defn m+1s-button []
  [:a.button.is-light.mr-1.mt-1
   {:on-click #(rf/dispatch [:set-timer-plus 1])}
   [:span "+1s"]])

(defn sound-button []
  (let [timer-sound (sub [:timer-sound])]
    (fn []
      [:a.button.is-light.mr-1.mt-1
       {:style {:color "red"}
        :on-click #(do (when @timer-sound (init-audio)
                                          (stop-alarm))
                       (rf/dispatch [:timer-sound]))}
       (if @timer-sound
         [:img {:src "volume-high-solid.svg"}]
         [:img {:src "volume-xmark-solid.svg"}])])))


(defn bel-slider [max-minutes data disabled]
  ;[:div.slidecontainer
   [:input.bel-slider {:disabled disabled
                       :step      60
                       ;:orient :vertical
                       ;:min -999 :max 999
                       :min       60
                       :max       max-minutes
                       :type      "range"
                       :value     (long @data)
                       :on-change #(let [val (-> % .-target .-value)]
                                     ;(println val)
                                     ;(set! (-> % .-target .-value) val)
                                     ;(swap! data assoc :range val)
                                     (rf/dispatch [:set-timer-duration-and-remaining-secs val]))}])


(defn format-01 [num]
  (if (> num 9) (str num) (str "0" num)))


(defn show-walltime-of [time-atom with-seconds]
  (if-let [time @time-atom]
    (let [seconds (if (= :with-seconds with-seconds)
                    (str ":" (format-01 (.getSeconds time)))
                    "")]
      (str (format-01 (.getHours time)) ":" (format-01 (.getMinutes time)) seconds))
    "???"))


(defn timer-comp
  "how many minutes can the user set the timer duration at max?"
  [max-minutes]
  (let [time        (sub [:timer-time])
        start       (sub [:timer-start])
        end         (sub [:timer-end])
        duration    (sub [:timer-duration-secs])
        remaining   (sub [:timer-remaining-secs])
        status      (sub [:timer-state])
        sound-on    (sub [:timer-sound])]
    (fn []
     (let [running (keyword-identical? @status :running)
           warning (and running (< 0 @remaining 60))
           alarm   (and running (< -60 @remaining 3))]

      [:div.timer-frame (when (-> alarm
                                  (and (odd? @remaining)))
                          {:style {:background "red"}})

       [:div.columns.is-centered
        [:div.column
         (when (not running)
           [:div {:style {:padding-left 20 :padding-right 20}}
            [bel-slider max-minutes duration running]])]]

       [:div.columns.is-centered
        (let [remainingDisplay (if (> @remaining 60) (str (quot @remaining 60)":" (format-01 (mod @remaining 60)))
                                                     (str @remaining "s"))
              durationDisplay (if (> @duration 60) (quot @duration 60) (str @duration "s"))]
          (if running
            [:div.column.is-full.has-text-centered [sound-button][stop-button][re-start-button durationDisplay]]
            [:div.column.is-full.has-text-centered
             [sound-button][m-1s-button][m-1-button][m-5-button][m20-button]
             [m+5-button][m+1-button][m+1s-button]
             [start-button durationDisplay]
             (when (and (not= @duration @remaining)
                        (> @remaining 0))
               [resume-button remainingDisplay])]))]

       [:div.columns.is-centered
        (when running
          [:div.column.is-full.has-text-centered
           [:font.wallclocktime
            (show-walltime-of start :without-seconds)
            " ⮕ " (show-walltime-of time :with-seconds)
            " ⮕ " (show-walltime-of end :without-seconds)]])]

       [timer 400  remaining duration status]

       (when (and
               @sound-on
               alarm
               (= @remaining 0))
         (play-alarm))

       (when-not running
         [:div.columns
            ;[:div.column]
            [:div.column.is-full.has-text-centered
             [:font.wallclocktime
              (show-walltime-of start :without-seconds)
              " ⮕ " (show-walltime-of time :with-seconds)
              " ⮕ " (show-walltime-of end :without-seconds)]]])

       [:br][:br][:br]

       [:div.columns
         [:div.column.is-full.has-text-centered
          [:p [:strong "Help"]]
          [:p "buttons, slider - set the total time."]
          [:p "mouse drag, touch the circle - set the current time."]
          [:p "start - (re)starts from the total time set."]
          [:p "resume - does not restart from total but from current."]
          [:p "ENTER - (re)starts the timer"]
          [:p "SPACE - stops or resumes the timer"]
          [:p "RIGHT/LEFT - increase or decrease total time by 1 minute"]
          [:p "UP/DOWN - increase or decrease total time by 1 second"]]]

       [:br][:br]

       [:div.columns
        [:div.column.is-full.has-text-centered
         [:p "created by BEL"]
         [:p "sources: "
          [:a {:href "https://github.com/bennoloeffler/wet"}
           "github.com/bennoloeffler/wet"]]]]]))))

;TODO:
; cofx with local store
;   read :timer-sound
;        :timer-duration-secs
;        :timer-remaining-secs
; from local store

(rf/reg-event-db
  :timer-init
  (fn [db [_ secs]]
    (-> db
        (assoc :timer-duration-secs secs)
        (assoc :timer-remaining-secs secs)
        (assoc :timer-state :stopped))))


(rf/reg-event-db ;; usage:  (rf/dispatch [:timer a-js-Date])
  :timer-time
  (fn [db [_ new-time]] ;; <-- de-structure the event vector
    (let [remaining (if (= :running (:timer-state db))
                      (subtract-in-secs (:timer-end db) new-time)
                      (:timer-remaining-secs db))
          end       (if (= :running (:timer-state db))
                      (:timer-end db)
                      (add-seconds remaining))
          end-map   (if end
                      {:timer-end end}
                      {})]
      (-> db
          (into end-map)
          (assoc :timer-time new-time)
          (assoc :timer-remaining-secs remaining)))))


(rf/reg-event-db
  :set-timer-duration-secs
  (fn [db [_ secs]]
    (assoc db :timer-duration-secs (long secs))))


(rf/reg-event-db
  :set-timer-percent
  (fn [db [_ percent]]
    ;(println "percent: " percent)
    (let [all (db :timer-duration-secs)
          remaining  (-> all (* percent) (/ 100))]
          ;_ (println remaining)]
      (assoc db :timer-remaining-secs (long remaining)))))


(rf/reg-event-db
  :set-timer-plus
  (fn [db [_ secs]]
    (let [test (+ secs (:timer-duration-secs db))]
      (if (and (= :stopped (db :timer-state))
               (> test 0)
               (< test (+ (* 60 120) 1)))
        (-> db
            (update :timer-duration-secs #(+ % secs))
            (update :timer-remaining-secs #(+ % secs)))
        db))))


(rf/reg-event-db
  :set-timer-duration-and-remaining-secs
  (fn [db [_ secs]]
    (-> db
        (assoc :timer-duration-secs (long secs))
        (assoc :timer-remaining-secs (long secs)))))


(rf/reg-event-db
  :timer-start
  (fn [db _]
   (if-not (= :running (db :timer-state))
    (let [now    (js/Date.)
          remain (db :timer-duration-secs)
          end    (add-seconds remain)]
      (-> db
          (assoc :timer-state :running)
          (assoc :timer-start now)
          (assoc :timer-end end)
          (assoc :timer-remaining-secs remain)))
    db)))


(rf/reg-event-db
  :timer-resume
  (fn [db [_ secs]]
    (assoc db :timer-state :running)))


(rf/reg-event-db
  :timer-stop
  (fn [db _]
    (-> db
        (assoc :timer-state :stopped))))


(rf/reg-event-fx
  :timer-re-start
  (fn [_ [_ secs]]
    {:fx [[:dispatch [:timer-stop]]
          [:dispatch [:set-timer-duration-and-remaining-secs secs]]
          [:dispatch [:timer-start]]]}))


(rf/reg-event-db
  :timer-toggle
  (fn [db _]
    (if (= :running (:timer-state db))
      (-> db
          (assoc :timer-state :stopped))
      (if (:timer-start db)
        (-> db
            (assoc :timer-state :running))
        db))))


(rf/reg-event-db
  :user-event
  (fn [db [_ evt]]
    (-> db
        (assoc :user-event evt #_(js-obj->clj-map evt)))))


(rf/reg-event-db
  :timer-sound
  (fn [db [_ evt]]
    (-> db
        (update :timer-sound not))))

;;
;; All subsriptions
;;

(rf/reg-sub
  :timer-duration-secs
  (fn [db _]
    (:timer-duration-secs db)))

(rf/reg-sub
  :timer-time
  (fn [db _]
    (:timer-time db)))

(rf/reg-sub
  :timer-start
  (fn [db _]
    (:timer-start db)))

(rf/reg-sub
  :timer-end
  (fn [db _]
    (:timer-end db)))

(rf/reg-sub
  :timer-remaining-secs
  (fn [db _]
    (:timer-remaining-secs db)))

(rf/reg-sub
  :timer-state
  (fn [db _]
    (:timer-state db)))

(rf/reg-sub
  :timer-sound
  (fn [db _]
    (:timer-sound db)))

(rf/reg-sub
  :user-event
  (fn [db _]
    (:user-event db)))