(ns sheepish-3d.into-the-void
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [sheepish-3d.map :as g]
            [sheepish-3d.engine :as engine]
            [goog.string :as gstring]
            [goog.string.format]))

(def unit 64)
(def world-size [512 384])

(defn initial-state
  []
  (let [[width height] world-size
        fov (/ q/PI 3)
        half-fov (* fov 0.5)
        ray-inc (/ fov width)
        rotation (- (* 0.3 q/PI))
        dir (+ rotation (* 0.5 q/PI))
        num-rays width]
    {:fov fov :half-fov (* fov 0.5) :world-size world-size :ray-inc ray-inc
     :rotation rotation :dir dir :res (/ q/PI 32) :pos [100 100]
     :num-rays num-rays :unit unit :pressed-keys #{} :accel 3
     :projection-dist (int (/ (/ width 2) (Math/tan half-fov)))}))

(defn setup
  []
  (q/smooth)
  (q/frame-rate 60)
  (initial-state))

(defn- draw-columns
  [{:keys [pos dir rotation unit world-size projection-dist] :as state}]
  (let [[width height] world-size]
    (doall
     (map-indexed
      (fn [i alpha]
        (let [[distance wall-type] (engine/find-intersect pos alpha unit)
              distance (engine/fishbowl-correction distance (- rotation alpha))
              wall-height (engine/wall-height distance unit projection-dist)]
          (q/with-fill (get g/color-map wall-type [255 184 108])
            (q/with-stroke (get g/color-map wall-type [255 184 108])
              (q/rect i (/ (- height wall-height) 2) 1 wall-height)))))
      (engine/ray-angles state)))))

(defn- draw-fps
  [state start]
  (let [end (gstring/format "%.1f" (/ 1.0 (* 0.001 (- (q/millis) start))))]
    (q/with-fill [255 184 108]
      (q/text "Current FPS: " 10 15)
      (q/text "  Target FPS: " 10 30))

    (q/with-fill [80 250 123]
      (q/text (q/current-frame-rate) 90 15)
      (q/text (q/target-frame-rate) 91 30))))

(defn draw
  [{:keys [world-size] :as state}]
  (let [start (q/millis)]

    ;; draw background
    (q/background 98 114 164)

    ;; draw floor
    (q/with-stroke [68 71 90]
      (q/with-fill [68 71 90]
        (q/rect 0 (/ (get world-size 1) 2)
                (get world-size 0) (get world-size 1))))

    (draw-columns state)
    (draw-fps state start)))

(defn move
  "Move player up/down"
  [{:keys [world-size accel pos pressed-keys rotation unit] :as state}]
  (let [[width height] world-size
        accel (if (contains? pressed-keys "shift") (* accel 1.5) accel)
        move-x (cond (contains? pressed-keys "w") +
                     (contains? pressed-keys "s") -)
        move-y (cond (contains? pressed-keys "w") -
                     (contains? pressed-keys "s") +)
        [x1 y1] pos]
    (if (and move-x move-y)
      (let [[x2 y2 :as p2] [(move-x x1 (* accel (q/cos rotation)))
                            (move-y y1 (* accel (q/sin rotation)))]]
        (update-in state [:pos] #(engine/hit-detection % p2 rotation (if (or (contains? pressed-keys "w")
                                                                             (contains? pressed-keys "up")) :up :down) unit)))
      state)))

(defn rotate
  "Rotate camera left/right"
  [{:keys [world-size accel pos pressed-keys rotation res] :as state}]
  (let [rotation-direction (cond (contains? pressed-keys "left") +
                                 (contains? pressed-keys "right") -)]
    (if rotation-direction
      (let [new-rotation (engine/normalize-angle (rotation-direction rotation res))]
        (-> state
            (update-in [:rotation] (constantly new-rotation))
            (update-in [:dir] #(engine/normalize-angle (+ new-rotation (* 0.5 q/PI))))))
      state)))

(defn strafe
  "Strafe player left/right"
  [{:keys [world-size accel pos pressed-keys rotation res dir unit] :as state}]
  (let [[width height] world-size
        accel (if (contains? pressed-keys "shift") (* accel 1.5) accel)
        move-x (cond (contains? pressed-keys "a") +
                     (contains? pressed-keys "d") -)
        move-y (cond (contains? pressed-keys "a") -
                     (contains? pressed-keys "d") +)
        [x1 y1] pos]
    (if (and move-x move-y)
      (let [[x2 y2 :as p2] [(move-x x1 (* accel (q/cos dir)))
                            (move-y y1 (* accel (q/sin dir)))]]
        (update-in state [:pos] #(engine/hit-detection % p2 dir (if (contains? pressed-keys "a") :left :right) unit)))
      state)))

(defn update-world
  [state]
  (-> state move rotate strafe))

(q/defsketch sheepish-3D
  :host "sheepish-3D"
  :draw draw
  :setup setup
  :key-pressed engine/key-pressed
  :key-released engine/key-released
  :update update-world
  :middleware [m/fun-mode]
  :size world-size)
