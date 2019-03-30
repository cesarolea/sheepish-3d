(ns sheepish-3d.map)

#_(def grid [[5 5 5 5 5 5 5 5 5 5 5 5]
           [5 0 0 0 0 0 0 0 0 0 0 5]
           [5 0 0 0 0 0 0 0 0 0 0 5]
           [5 0 0 0 0 0 0 0 0 0 0 5]
           [5 0 0 0 0 0 0 0 0 0 0 5]
           [5 0 0 0 0 0 0 0 0 0 0 5]
           [5 0 0 0 0 0 0 0 0 0 0 5]
           [5 5 5 5 5 5 5 5 5 5 5 5]])

(def grid [[1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 2 2 2 2 2 0 0 0 0 3 0 3 0 3 0 0 0 2]
           [2 0 0 0 0 0 2 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 2 0 0 0 2 0 0 0 0 3 0 0 0 3 0 0 0 2]
           [2 0 0 0 0 0 2 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 2 2 0 2 2 0 0 0 0 3 0 3 0 3 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 5 0 5 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 5 0 5 0 0 0 0 0 2]
           [2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 35 35 35 35 35 35 35 35 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 35 0 35 0 0 0 0 35 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 35 0 0 0 0 18 0 50 35 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 35 0 35 0 0 0 0 35 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 35 0 35 35 35 35 35 33 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 35 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 35 35 35 35 0 35 35 33 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2]
           [2 4 4 4 4 4 4 49 4 1 1 1 1 0 1 1 1 1 0 1 1 1 1 2]
           [4 4 4 4 4 4 4 4 4 1 1 1 1 6 1 1 1 1 6 1 1 1 1 2]])

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

(def map-size [(count (get grid 0)) (count grid)])

(defn grid-coord
  "Get the value corresponding to the grid coordinates"
  [x y]
  (-> grid (get y) (get x)))
