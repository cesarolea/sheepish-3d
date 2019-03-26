(ns sheepish-3d.maze-runner
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [sheepish-3d.engine :as engine]))

(def scale 64)
(def player-size [20 8])

(def grid [[1 1 1 1 1 1 1 1 1 1 1 1]
           [1 0 0 0 0 0 0 0 0 0 0 1]
           [1 0 0 3 3 0 3 3 3 0 0 1]
           [1 0 0 0 0 0 3 0 3 0 0 1]
           [1 0 0 3 0 0 3 0 3 3 3 1]
           [1 0 0 0 3 0 0 0 0 0 0 1]
           [1 0 0 0 0 0 0 0 0 3 3 1]
           [1 1 1 1 1 1 1 1 1 1 1 1]])

(def map-size [(count (get grid 0)) (count grid)])

(def color-map
  {1 [255 184 108] ;; orange
   2 [255 121 198] ;; pink
   3 [189 147 249] ;; purple
   4 [255 85 85]   ;; red
   5 [139 233 253] ;; cyan
   7 [255 121 198] ;; pink
   10 [189 147 249] ;; purple
   11 [255 85 85]   ;; red
   })

(defn coord->grid
  "Convert from cartesian coordinates to grid coordinates"
  [[px py] unit]
  [(Math/floor (/ px unit)) (Math/floor (/ py unit))])

(defn grid-value
  "Get the value corresponding to the grid coordinates"
  [x y]
  (-> grid (get y) (get x)))

(defn inside-map?
  "Checks if a point is outside the bounds of the current map"
  [point unit]
  (let [[x y] point
        width (* (get map-size 0) unit)
        height (* (get map-size 1) unit)]
    (and (<= 0 x width) (<= 0 y height))))

(defn wall?
  [point unit]
  (let [[px py] point
        [x y] (coord->grid point unit)]
    (or (not (inside-map? point unit))
        (> (grid-value x y) 0))))

(defn hit-detection
  "Receives p1 (origin) and p2 (destination) and returns a new point with it's (x,y) components
  not colliding with walls"
  [p1 p2 unit]
  (if (wall? p2 unit) p1 p2))

(defn- center-player-in-grid
  "Given a set of grid coords, return a coordinate so that the player
  is centered in that grid square"
  [x y]
  (let [[p-width p-height] player-size]
    [(+ (- (* scale x) (/ scale 2)) (/ p-width 2))
     (+ (- (* scale y) (/ scale 2)) (/ p-height 2))]))

(defn- h-intersect
  [pos angle unit]
  (let [[x y] pos
        a angle
        tan-a (Math/tan a)
        Ya (if (pos? a) (- unit) unit)
        Xa (if (zero? tan-a)
             Ya
             (/ Ya (Math/tan (- a))))
        Ay (if (pos? a)
             (* (Math/floor (/ y unit)) unit)
             (+ (* (Math/floor (/ y unit)) unit) unit))
        Ax (+ x (/ (- y Ay) tan-a))
        Ay (if (pos? a) (dec Ay) Ay)]
    (loop [Ax Ax Ay Ay]
      (if (wall? [Ax Ay] unit)
        [Ax Ay]
        (recur (+ Ax Xa) (+ Ay Ya))))))

(defn- v-intersect
  [pos angle unit]
  (let [[x y] pos
        a angle
        half-pi (/ Math/PI 2.0)
        tan-a (Math/tan a)
        Xa (if (< (- half-pi) angle half-pi) unit (- unit))
        Ya (* Xa (Math/tan (- a)))
        Bx (if (< (- half-pi) angle half-pi)
             (+ (* (Math/floor (/ x unit)) unit) unit)
             (* (Math/floor (/ x unit)) unit))
        By (+ y (* (- x Bx) (Math/tan a)))
        Bx (if (< (- half-pi) angle half-pi) Bx (dec Bx))]
    (loop [Bx Bx By By]
      (if (wall? [Bx By] unit)
        [Bx By]
        (recur (+ Bx Xa) (+ By Ya))))))

(defn distance
  "Calculate distance to wall"
  [[x y] [x-i y-i]]
  (Math/sqrt (+ (Math/pow (- x x-i) 2) (Math/pow (- y y-i) 2))))

(defn find-intersect
  [pos angle unit]
  (let [[x-h y-h :as h-int] (h-intersect pos angle unit)
        [x-v y-v :as v-int] (v-intersect pos angle unit)
        h-dist (distance pos h-int)
        v-dist (distance pos v-int)]
    (if (< h-dist v-dist) h-dist v-dist)))

(defn initial-state
  []
  (let [[width height :as world-size] (into [] (map #(* % scale) map-size))
        fov (/ q/PI 3)
        ray-inc (/ fov width)
        rotation q/PI
        dir (+ rotation (* 0.5 q/PI))
        num-rays width]
    {:fov fov :half-fov (* fov 0.5) :world-size world-size :ray-inc ray-inc
     :rotation rotation :dir dir :res (/ q/PI 32) :pos (center-player-in-grid 10 6)
     :num-rays num-rays :unit scale :pressed-keys #{} :accel 3}))

(defn setup
  []
  (q/smooth)
  (q/frame-rate 60)
  (initial-state))

(defn- draw-grid
  [{:keys [world-size unit] :as state}]
  (q/with-stroke [80 120]

    ;; draw vertical lines
    (doseq [vert (range 1 (/ (get world-size 0) unit))]
      (let [[_ height] world-size
            [x1 y1 :as p1] [(* vert unit) 0]
            [x2 y2 :as p2] [x1 height]]
        (q/line p1 p2)))

    ;; draw horizontal lines
    (doseq [hor (range 1 (/ (get world-size 1) unit))]
      (let [[width _] world-size
            [x1 y1 :as p1] [0 (* hor unit)]
            [x2 y2 :as p2] [width y1]]
        (q/line p1 p2)))))

(defn- draw-walls
  [{:keys [unit] :as state}]
  (loop [row 0]
    (let [current-row (get grid row)]
      (when current-row
        (loop [column 0]
          (let [current-column (get current-row column)]
            (when current-column
              (when (> current-column 0)
                (q/with-stroke (get color-map current-column)
                  (q/with-fill (get color-map current-column)
                    (q/rect (* column unit) (* row unit) 64 64))))
              (recur (inc column)))))
        (recur (inc row))))))

(defn- player-triangle
  [{:keys [pos dir unit rotation] :as state}]
  (let [[x1 y1 :as p1] pos
        p-length 20 p-width 8
        [xl yl :as pl] [(- x1 (* p-length (q/cos rotation))) (+ y1 (* p-length (q/sin rotation)))]
        [x2 y2 :as p2] [(+ xl (* p-width (q/cos dir))) (- yl (* p-width (q/sin dir)))]
        [x3 y3 :as p3] [(- xl (* p-width (q/cos dir))) (+ yl (* p-width (q/sin dir)))]]
    [p1 p2 p3]))

(defn- draw-player
  "Draw the player as a rectangle with the acute angle pointing towards
  the rotation"
  [{:keys [pos dir unit world-size rotation] :as state}]
  (let [[[x1 y1] [x2 y2] [x3 y3]] (player-triangle state)]
    (q/with-fill [255 85 85]
      (q/with-stroke [255 85 85]
        (q/triangle x1 y1 x2 y2 x3 y3)))))

(defn- draw-rays
  [{:keys [pos dir rotation unit] :as state}]
  (doall
   (map-indexed
    (fn [i alpha]
      (let [length (find-intersect pos alpha unit)
            [x1 y1] pos
            [x2 y2 :as p2] [(+ x1 (* length (q/cos alpha)))
                            (- y1 (* length (q/sin alpha)))]]
        (q/with-stroke (if (even? i) [80 250 123] [40 42 54])
          (q/line pos p2))))
    (engine/ray-angles state))))

(defn key-pressed
  [{:keys [pressed-keys] :as state} {:keys [key key-code] :as event}]
  (let [pressed-keys (into #{} (conj pressed-keys (.toLowerCase (name key))))]
    (update-in state [:pressed-keys] (constantly pressed-keys))))

(defn key-released
  [{:keys [pressed-keys] :as state} {:keys [key key-code] :as event}]
  (let [pressed-keys (into #{} (disj pressed-keys (.toLowerCase (name key))))]
    (update-in state [:pressed-keys] (constantly pressed-keys))))

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
           (update-in state [:pos] #(hit-detection % p2 unit)))
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
        (update-in state [:pos] #(hit-detection % p2 unit)))
      state)))

(defn debug
  [{:keys [world-size accel pos pressed-keys rotation res dir unit] :as state}]
  (if (contains? pressed-keys " ")
    (do
      (println state)
      state)
    state))

(defn draw
  [state]

  ;; draw background
  (q/background 98 114 164)
  (draw-walls state)
  (draw-grid state)
  (draw-player state)
  (draw-rays state))

(defn update-world
  [state]
  (-> state
      move
      rotate
      strafe
      debug))

(q/defsketch maze-runner
  :host "maze-runner"
  :draw draw
  :setup setup
  :key-pressed key-pressed
  :key-released key-released
  :update update-world
  :middleware [m/fun-mode]
  :size (into [] (map #(* % scale) map-size)))
