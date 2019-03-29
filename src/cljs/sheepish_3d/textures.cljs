(ns sheepish-3d.textures
  (:require [quil.core :as q :include-macros true]))

(defn get-texture-offset
  "Calculate the correct offset to load a texture"
  [texture-number texture-slice]
  (let [row (Math/ceil (/ texture-number 3))
        x-offset (condp = (rem texture-number 3)
                   1 0
                   2 128
                   0 256)]
    [(+ x-offset texture-slice) (int (* (dec row) 64))]))
