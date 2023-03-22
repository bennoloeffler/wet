(ns wet.validation
  (:require [struct.core :as st]))

(defn percentage [width length  x y]
  ;(println " w:" width " l:" length " x:" x " y:" y)
  (let [inner-square (min width length)
        w2 (/ width 2)
        l2 (/ length 2)
        x* (- x w2)
        y* (- y l2)
        angle (Math/atan2 y* x*) ; - counterclock
        rotated (+ angle (/ Math/PI 2)) ; start on top
        pi2 (* 2 Math/PI)
        result (-> rotated
                   (* 100)
                   (/ pi2))
        corr (if (neg? result)
               (+ 100 result)
               result)]
    corr))



(comment

  ; 25% (oder 75%) ?
  ; w: 399  l: 782.5  x: 379  y: 389
  (percentage 399 782.5 379 (/ 782.5 2))
  ; clockwise

  ; middle top = 0 = 0%
  (percentage 300 300 150 0)
  (percentage 300 800 150 0)

  ; right top = 45 = 12,5%
  (percentage 300 300 300 0)
  (percentage 300 800 300 0)

  ; right middle = 90 = 25%
  (percentage 300 300 300 150)


  ; right down = 135 = 37,5%
  (percentage 300 300 300 300)

  ; middle down = 90 = 75%
  (percentage 300 300 0 150)

  ; middle bottom = 180 = 50%
  (percentage 300 300 150 300)

  ; less than 25% -> corrected!
  (percentage 300 300 300 140)


  (percentage 300 300 153 153)
  (percentage 300 300 149 149)

  ; ??? singularity works... due to atan2
  (percentage 300 300 150 150)

  nil)
