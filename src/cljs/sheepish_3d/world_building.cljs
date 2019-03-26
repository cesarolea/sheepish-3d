(ns sheepish-3d.world-building
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

(defn initial-state
  []
  (let [[width height :as world-size] [512 320]
        fov (/ q/PI 3)
        ray-inc (/ fov width)
        rotation (/ q/PI 2)
        dir (+ rotation (* 0.5 q/PI))
        num-rays width]
    {:fov fov :half-fov (* fov 0.5) :world-size world-size :ray-inc ray-inc
     :rotation rotation :dir dir :res (/ q/PI 64) :pos [(/ width 2) (+ (/ height 2) 50)]
     :num-rays num-rays}))

(defn setup
  []
  (q/smooth)
  (q/frame-rate 60)
  (initial-state))

(defn draw
  [{:keys [fov half-fov ray-inc world-size rotation pos dir num-rays] :as state}]

  ;; draw background
  (q/background 98 114 164)

  (let [[width height] world-size
        [x1 y1 :as p1] pos
        ray-angle (+ rotation half-fov)]

    (q/with-stroke [80 120]
      ;; shoot rays
      (doseq [n-ray (range 1 (inc num-rays))]
        (let [ray-angle (normalize-angle (- ray-angle (* n-ray ray-inc)))
              [x2 y2 :as p2] [(+ x1 (* 200 (q/cos ray-angle)))
                              (- y1 (* 200 (q/sin ray-angle)))]]
          (q/line p1 p2))))

    ;; rotation line
    (q/stroke 255 85 85)
    (q/line p1 [(+ x1 (* 100 (q/cos rotation)))
                (- y1 (* 100 (q/sin rotation)))])

    ;; direction line
    (q/stroke 241 250 140)
    (q/line p1 [(+ x1 (* 50 (q/cos dir)))
                (- y1 (* 50 (q/sin dir)))])

    ;; center dot (origin)
    (q/stroke 255 85 85)
    (q/fill 255 85 85)
    (q/ellipse x1 y1 2 2)

    ; stats
    (q/fill 255 184 108)
    (q/text "FOV: " 10 15)
    (q/text "Ray inc: " 10 30)
    (q/text "Ray count: " 10 45)
    (q/text "Direction: " 10 60)
    (q/text "Position: " 10 75)

    (q/fill 80 250 123)
    (q/text fov 80 15)
    (q/text ray-inc 80 30)
    (q/text num-rays 80 45)
    (q/text rotation 80 60)
    (q/text (str (int x1) " " (int y1)) 80 75)))

(defn key-pressed
  [{:keys [rotation res fov world-size pos num-rays] :as state} {:keys [key-code]}]
  (case key-code

    ;; num rays
    (75 74) (let [num-rays (if (= key-code 75) (/ num-rays 2) (* num-rays 2))
                  ray-inc (/ fov num-rays)]
              (-> state
                  (update-in [:num-rays] (constantly num-rays))
                  (update-in [:ray-inc] (constantly ray-inc))))

    ;; position up / down
    (87 83) (let [[width height] world-size
                  accel 5
                  [x1 y1] pos
                  [x2 y2] [((if (= key-code 87) + -) x1 (* accel (q/cos rotation)))
                           ((if (= key-code 87) - +) y1 (* accel (q/sin rotation)))]]
              (update-in state [:pos]
                         (constantly [(if (or (< x2 0)
                                              (>= x2 width)) x1 x2)
                                      (if (or (< y2 0)
                                              (>= y2 height)) y1 y2)])))

    ;; strafe left / right
    (65 68) (let [[width height] world-size
                  accel 5
                  strafe-dir (normalize-angle (+ rotation (* 0.5 q/PI)))
                  [x1 y1] pos
                  [x2 y2] [((if (= key-code 65) + -) x1 (* accel (q/cos strafe-dir)))
                           ((if (= key-code 65) - +) y1 (* accel (q/sin strafe-dir)))]]
              (update-in state [:pos]
                         (constantly [(if (or (< x2 0)
                                              (>= x2 width)) x1 x2)
                                      (if (or (< y2 0)
                                              (>= y2 height)) y1 y2)])))

    ;; rotate left / right
    (37 39) (let [new-rotation (normalize-angle ((if (= key-code 37) + -) rotation res))
                  new-dir (normalize-angle (+ new-rotation (* 0.5 q/PI)))]
              (-> state
                  (update-in [:rotation] (constantly new-rotation))
                  (update-in [:dir] (constantly new-dir))))

    ;; increase / decrease fov
    (38 40) (let [fov ((if (= key-code 38) + -) fov (* 0.02 q/PI))]
              (-> state
                  (update-in [:fov] (constantly fov))
                  (update-in [:half-fov] #(* fov 0.5))
                  (update-in [:ray-inc] #(/ fov num-rays))))

    ;; reset
    32 (initial-state)
    state))

(q/defsketch world-building
  :host "world-building"
  :draw draw
  :setup setup
  :key-pressed key-pressed
  :middleware [m/fun-mode]
  :size [512 320])
