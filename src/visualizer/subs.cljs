(ns visualizer.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::v
 (fn [db]
   (:v db)))

(re-frame/reg-sub
 ::resv
 (fn [db]
   (:resv db)))

(re-frame/reg-sub
 ::idx
 (fn [db]
   (:idx db)))
(re-frame/reg-sub
 ::hof
 (fn [db]
   (:hof db)))

(re-frame/reg-sub
::f (fn [db] (:f db)) )