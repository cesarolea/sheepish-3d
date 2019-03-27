(ns sheepish-3d.engine
  (:require [quil.core :as q :include-macros true]
            [sheepish-3d.map :as g]))

(defn key-pressed
  [{:keys [pressed-keys] :as state} {:keys [key key-code] :as event}]
  (let [pressed-keys (into #{} (conj pressed-keys (.toLowerCase (name key))))]
    (update-in state [:pressed-keys] (constantly pressed-keys))))

(defn key-released
  [{:keys [pressed-keys] :as state} {:keys [key key-code] :as event}]
  (let [pressed-keys (into #{} (disj pressed-keys (.toLowerCase (name key))))]
    (update-in state [:pressed-keys] (constantly pressed-keys))))

(defn grid-value
  "Get the value corresponding to the grid coordinates"
  [x y]
  (-> g/grid (get y) (get x)))

(defn coord->grid
  "Convert from cartesian coordinates to grid coordinates"
  [[px py] unit]
  [(Math/floor (/ px unit)) (Math/floor (/ py unit))])

(defn inside-map?
  "Checks if a point is outside the bounds of the current map"
  [point unit]
  (let [[x y] point
        width (* (get g/map-size 0) unit)
        height (* (get g/map-size 1) unit)]
    (and (<= 0 x width) (<= 0 y height))))

(defn wall?
  [point unit]
  (let [[px py] point
        [x y] (coord->grid point unit)]
    (or (not (inside-map? point unit))
        (> (grid-value x y) 0))))

(defn get-cell-type
  [point unit]
  (let [[px py] point
        [x y] (coord->grid point unit)]
    (when (inside-map? point unit)
      (grid-value x y))))

(defn hit-detection
  "Receives p1 (origin) and p2 (destination) and returns a new point with it's (x,y) components
  not colliding with walls"
  [[x1 y1] [x2 y2] length unit]
  (let [xf (if (wall? [x2 y1] unit) x1 x2)
        yf (if (wall? [x1 y2] unit) y1 y2)]
    [xf yf]))

(defn normalize-angle
  "Given an angle (in radians), return a corresponding angle that is normalized between 2π - π
  The sign represents the angle direction."
  [α]
  (let [π-sqr (* 2 Math/PI)
        α (mod α π-sqr) ;; make angle between 0 -- 2π
        ]
    (if (> α Math/PI)
      (- α π-sqr)  ;; make angle between -π -- π
      α)))

(defn- h-intersect
  [pos angle unit]
  (let [[x y] pos
        tan-a (Math/tan angle)
        Ya (if (pos? angle) (- unit) unit)
        Xa (if (zero? tan-a) Ya (/ Ya (Math/tan (- angle))))
        Ay (if (pos? angle)
             (* (Math/floor (/ y unit)) unit)
             (+ (* (Math/floor (/ y unit)) unit) unit))
        Ax (+ x (/ (- y Ay) tan-a))
        Ay (if (pos? angle) (dec Ay) Ay)]
    (loop [Ax Ax Ay Ay]
      (if (wall? [Ax Ay] unit)
        [Ax Ay]
        (recur (+ Ax Xa) (+ Ay Ya))))))

(defn- v-intersect
  [pos angle unit]
  (let [[x y] pos
        half-pi (/ Math/PI 2.0)
        Xa (if (< (- half-pi) angle half-pi) unit (- unit))
        Ya (* Xa (Math/tan (- angle)))
        Bx (if (< (- half-pi) angle half-pi)
             (+ (* (Math/floor (/ x unit)) unit) unit)
             (* (Math/floor (/ x unit)) unit))
        By (+ y (* (- x Bx) (Math/tan angle)))
        Bx (if (< (- half-pi) angle half-pi) Bx (dec Bx))]
    (loop [Bx Bx By By]
      (if (wall? [Bx By] unit)
        [Bx By]
        (recur (+ Bx Xa) (+ By Ya))))))

(defn fishbowl-correction
  "Implement fishbowl correction to calculated distance. To remove the viewing distortion,
  the resulting distance must be multiplied by cos(BETA); where BETA is the angle of the ray that is
  being cast relative to the viewing angle."
  [dist beta]
  (* dist (Math/cos beta)))

(defn distance
  "Calculate distance to wall"
  [[x y] [x-i y-i]]
  (Math/sqrt (+ (Math/pow (- x x-i) 2) (Math/pow (- y y-i) 2))))

(defn wall-height
  "Calculate wall height according to the wall distance"
  [distance unit projection-distance]
  (Math/ceil (* (/ unit distance) projection-distance)))

(defn find-intersect
  [pos angle unit]
  (let [[x-h y-h :as h-int] (h-intersect pos angle unit)
        [x-v y-v :as v-int] (v-intersect pos angle unit)
        h-dist (distance pos h-int)
        v-dist (distance pos v-int)]
    (if (< h-dist v-dist)
      [h-dist (get-cell-type h-int 64)]
      [v-dist (get-cell-type v-int 64)])))

(defn ray-angles
  "Generate a list of angles (in radians) to use for rays"
  [{:keys [half-fov ray-inc world-size rotation] :as state}]
  (let [[width height] world-size
        ray-angle (+ rotation half-fov)]
    (map (fn [n-ray] (normalize-angle (- ray-angle (* n-ray ray-inc))))
         (range 1 (inc width)))))
