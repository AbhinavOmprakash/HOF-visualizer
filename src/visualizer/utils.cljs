;; taken from https://gist.github.com/swannodette/5882703

(ns visualizer.utils
  (:require [cljs.core.async :refer [chan close!]])
  )
 
(defn timeout [ms]
  (let [c (chan)]
    (js/setTimeout (fn [] (close! c)) ms)
    c))
 
