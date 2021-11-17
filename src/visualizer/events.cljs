(ns visualizer.events
  (:require
   [re-frame.core :as re-frame]
   [visualizer.db :as db]
   [visualizer.subs :as subs]
   [visualizer.utils :as utils]
   [cljs.reader :as reader]
   [clojure.core.async :refer [<!]])
  (:require-macros
   [cljs.core.async.macros :as m :refer [go]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(defn pop-data [coeffects _]
  (let [new-db (dissoc (:db coeffects) :v)]
         {:db (assoc new-db :v (vec (take 10 (repeatedly #(rand-int 50)))))}))
        

(re-frame/reg-event-fx
 :populate-data
 pop-data)

(defn start-comp [coeffects _]
  (let [idx (:idx (:db coeffects))
        new-db (dissoc (:db coeffects) :idx)
        inv (:v (:db coeffects))]
    
    (if (< (inc idx) (count inv))
      (do
        (re-frame/dispatch [:compute])
        {:db (assoc new-db :idx (inc idx))})
      {:db (assoc new-db :idx (inc idx))})))

(defn reset-idx [coeffects _]
  (let [new-db (dissoc (:db coeffects) :idx)]
    {:db (assoc new-db :idx -1)}))

(defn reset-resv [coeffects _]
  (let [new-db (dissoc (:db coeffects) :resv)
        hof @(re-frame/subscribe [::subs/hof])]
    (if (= hof reduce)
    {:db (assoc new-db :resv 0)}
    {:db (assoc new-db :resv [])}
      )))

(defmulti compute (fn [_ _] 
                    @(re-frame/subscribe [::subs/hof])))

(defmethod compute map [coeffects _]
  (let [db (:db coeffects)
        inv (:v db)
        resv (:resv db)
        f (:f db)
        i (:idx db)
        new-db (dissoc db resv)
        el (get inv i)
        new-resv (conj resv (str (inc el)))]
    (go
      (<! (utils/timeout 750))
      (re-frame/dispatch [:start]))
    {:db (assoc new-db :resv new-resv)}))

(defmethod compute filter [coeffects _]
  (let [db (:db coeffects)
        inv (:v db)
        resv (:resv db)
        f (:f db)
        i (:idx db)
        new-db (dissoc db resv)
        el (get inv i)
        ; filter step
        new-resv (if (even? el)
                   (conj resv el)
                   resv)]
    (go
      (<! (utils/timeout 750))
      (re-frame/dispatch [:start]))
    {:db (assoc new-db :resv new-resv)}))

(defmethod compute reduce [coeffects _]
  (let [db (:db coeffects)
        inv (:v db)
        resv (:resv db)
        f (:f db)
        i (:idx db)
        new-db (dissoc db resv)
        el (get inv i)
        ; filter step
        new-resv (+ resv el)]
    (go
      (<! (utils/timeout 750))
      (re-frame/dispatch [:start]))
    {:db (assoc new-db :resv new-resv)}))


(defn change-hof [coeffects event]
  (let [hof (second event)
        db (:db coeffects)
        new-db (dissoc db :hof)]
    {:db (assoc new-db :hof hof)}))

(defn reset-inv [coeffects _]
  (let [new-db (dissoc (:db coeffects) :v)]
    {:db (assoc new-db :v [])}))

;; (defn parse-input [coeffects event]
;;   (let [in-expr (second event)
;;         db (:db coeffects)
;;         expr (reader/read-string in-expr)
;;         [hof f v] expr
;;         new-db (-> db
;;                    (dissoc :v)
;;                    (dissoc :hof)
;;                    (dissoc :f))
;;         ret-db (-> new-db
;;                    (assoc :v v)
;;                    (assoc :hof hof)
;;                    (assoc :f f))]
;;     (js/alert (str ret-db))

;;     {:db ret-db}))

(re-frame/reg-event-fx
 :start
 start-comp)

;; (re-frame/reg-event-fx
;;  :parse-input
;;  parse-input)

(re-frame/reg-event-fx
 :reset-idx
 (comp reset-idx reset-resv))

(re-frame/reg-event-fx
 :compute
 compute)

(re-frame/reg-event-fx
 :change-hof
 change-hof)

(re-frame/reg-event-fx
 :reset-everything
 (comp reset-idx reset-resv reset-inv))
