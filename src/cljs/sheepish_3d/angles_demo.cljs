(ns sheepish-3d.angles-demo
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn normalize-angle
  "Given an angle (in radians), return a corresponding angle that is normalized between 2π - π
  The sign represents the angle direction."
  [α]
  (let [π-sqr (* 2 q/PI)
        α (mod α π-sqr) ;; make angle between 0 -- 2π
        ]
    (if (> α q/PI)
      (- α π-sqr)  ;; make angle between -π -- π
      α)))

(defn setup []
  (do (q/smooth)
      (q/frame-rate 60)
      {:angle (normalize-angle (* 0.25 q/PI)) :res (/ q/PI 64)}))

(defn draw
  [{:keys [angle] :as state}]
  (let [x1 (/ 512 2)
        y1 (/ 320 2)
        length y1
        x2 (+ x1 (* length (q/cos angle)))
        y2 (- y1 (* length (q/sin angle)))]

    ;; background
    (q/background 98 114 164)

    ;; x axis
    (q/stroke 248 248 242)
    (q/line [0 length] [512 length])

    ;; radar
    (q/stroke 248 248 242)
    (q/fill 98 114 164)
    (q/ellipse x1 y1 320 320)

    ;; center dot (origin)
    (q/stroke 255 85 85)
    (q/fill 255 85 85)
    (q/ellipse x1 y1 2 2)

    ;; hypotenuse
    (q/stroke 255 85 85)
    (q/line [x1 y1] [x2 y2])

    (q/stroke 255 184 108)
    ;; opposite
    (q/line [x2 y2] [x2 y1])

    ;; adjacent
    (q/line [x1 y1] [x2 y1])

    ;; draw angle indicator
    (q/fill 255 184 108)
    (q/text "Ray direction: " 10 15)
    (q/text "Angle value: " 10 30)
    (q/text "Quadrant: " 10 45)
    (if (pos? angle) (q/fill 80 250 123) (q/fill 255 121 198))
    (q/text (if (pos? angle) "Up" "Down") 90 15)
    (q/fill 80 250 123)
    (q/text angle 90 30)
    (q/text (cond
              (and (>= angle 0) (< angle (/ q/PI 2))) "I"
              (and (>= angle (/ q/PI 2)) (< angle q/PI)) "II"
              (and (>= (+ angle (* 2 q/PI)) q/PI) (< (+ angle (* 2 q/PI)) (* 1.5 q/PI))) "III"
              (>= (+ angle (* 2 q/PI)) (* 1.5 q/PI)) "IV")
            90 45)))

(defn key-pressed
  [{:keys [angle res] :as state} {:keys [key key-code]}]
  (case key
    (:up :right) (update-in state [:angle] #(normalize-angle (+ % res)))
    (:down :left) (update-in state [:angle] #(normalize-angle (- % res)))
    state))

(q/defsketch angles-demo
  :host "angles-demo"
  :draw draw
  :setup setup
  :key-pressed key-pressed
  :middleware [m/fun-mode]
  :size [512 320])
