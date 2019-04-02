(ns sheepish-3d.textures
  (:require [quil.core :as q :include-macros true]))

(defn get-texture-offset
  "Calculate the correct offset to load a texture"
  [texture-number texture-slice direction]
  (let [thin-walls #{\V \H}
        texture-number (if (contains? thin-walls texture-number) 50 texture-number)
        row (Math/ceil (/ texture-number 3))
        x-offset (condp = (rem texture-number 3)
                   1 0
                   2 128
                   0 256)
        x-offset (cond
                   (= direction :h) x-offset
                   (= direction :v) (+ x-offset 64)
                   :else x-offset)]
    [(+ x-offset texture-slice) (int (* (dec row) 64))]))
