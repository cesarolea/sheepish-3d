(ns sheepish-3d.engine
  (:require [quil.core :as q :include-macros true]
            [sheepish-3d.map :as g]
            [sheepish-3d.textures :as t]))

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
  (let [[x y] (coord->grid point unit)]
    (or (not (inside-map? point unit))
        (> (grid-value x y) 0))))

(defn thin-hor-wall?
  [point unit]
  (let [[x y] (coord->grid point unit)]
    (or (not (inside-map? point unit))
        (= (grid-value x y) \H))))

(defn thin-ver-wall?
  [point unit]
  (let [[x y] (coord->grid point unit)]
    (or (not (inside-map? point unit))
        (= (grid-value x y) \V))))

(defn get-cell-type
  [point unit]
  (let [[px py] point
        [x y] (coord->grid point unit)]
    (when (inside-map? point unit)
      (grid-value x y))))

(defn hit-detection
  "Receives p1 (origin) and p2 (destination) and returns a new point with it's (x,y) components
  not colliding with walls"
  [[x1 y1] [x2 y2] angle move-dir unit]
  (let [half-pi (* 0.5 Math/PI)
        move-y (if (pos? angle) (if (or (= move-dir :left)
                                        (= move-dir :up)) - +) (if (or (= move-dir :left)
                                                                       (= move-dir :up)) + -))
        move-x (if (< (- half-pi) angle half-pi) (if (or (= move-dir :left)
                                                         (= move-dir :up)) + -) (if (or (= move-dir :left)
                                                                                        (= move-dir :up)) - +))
        half-unit (* 0.5 unit)
        [xa ya] [(move-x x2 half-unit) (move-y y2 half-unit)]
        xf (if (or (wall? [xa y1] unit)
                   (thin-hor-wall? [xa y1] unit)
                   (thin-ver-wall? [xa y1] unit)) x1 x2)
        yf (if (or (wall? [x1 ya] unit)
                   (thin-hor-wall? [x1 ya] unit)
                   (thin-ver-wall? [x1 ya] unit)) y1 y2)]
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
      (cond
        ;; a regular wall
        (wall? [Ax Ay] unit) [(Math/floor Ax) (Math/floor Ay)]
        ;; a thin horizontal wall
        (thin-hor-wall? [Ax Ay] unit) [(Math/floor (+ Ax (* 0.2 Xa))) (Math/floor (+ Ay (* 0.2 Ya)))]
        ;; no wall, extend the ray
        :else (recur (+ Ax Xa) (+ Ay Ya))))))

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
      (cond
        ;; a regular wall
        (wall? [Bx By] unit) [(Math/floor Bx) (Math/floor By)]
        ;; a thin vertical wall
        (thin-ver-wall? [Bx By] unit) [(Math/floor (+ Bx (* 0.2 Xa))) (Math/floor (+ By (* 0.2 Ya)))]
        ;; no wall, extend the ray
        :else (recur (+ Bx Xa) (+ By Ya))))))

(defn fishbowl-correction
  "Implement fishbowl correction to calculated distance. To remove the viewing distortion,
  the resulting distance must be multiplied by cos(BETA); where BETA is the angle of the ray that is
  being cast relative to the viewing angle."
  [dist beta]
  (Math/round (* dist (Math/cos beta))))

(defn wall-height
  "Calculate wall height according to the wall distance"
  [distance unit projection-distance]
  (Math/ceil (* (/ unit distance) projection-distance)))

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
    (if (< h-dist v-dist)
      [h-dist (get-cell-type h-int 64) (Math/floor (mod x-h unit)) :h]
      [v-dist (get-cell-type v-int 64) (Math/floor (mod y-v unit)) :v])))

(defn ray-angles
  "Generate a list of angles (in radians) to use for rays"
  [{:keys [half-fov ray-inc world-size rotation] :as state}]
  (let [[width height] world-size
        ray-angle (+ rotation half-fov)]
    (map (fn [n-ray] (normalize-angle (- ray-angle (* n-ray ray-inc))))
         (range 1 (inc width)))))

(def ray-angles-m (memoize ray-angles))
