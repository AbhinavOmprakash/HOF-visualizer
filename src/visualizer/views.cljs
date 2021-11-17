(ns visualizer.views
  (:require
   [re-frame.core :as re-frame]
   [visualizer.subs :as subs]
   [reagent.core :as r]
   ))
   
(defn v->hic
  "returns vector elements with element at idx highlighted."
  [idx v]
  (if (< idx 0)
    [:h1.title.is-1 (str v)]
    [:h1.title.is-1.in-vector (->> (for [i (range (count v))]
                           (if (= i idx)
                             [:text.highlight (get v i)]
                             [:text (get v i)]))
                         (interpose " ")
                         (cons [:text "["])
                         (#(concat % [[:text "]"]])))]))


(defn data-v []
  (let [v @(re-frame/subscribe [::subs/v])
        i @(re-frame/subscribe [::subs/idx])]
    [:div.columns.is-centered
     [:div.column.is-quarter
      [:h1.title.is-1
       (v->hic i
               v)]]]))

(defmulti res (fn [] @(re-frame/subscribe [::subs/hof])))

(defmethod res map []
  (let [res @(re-frame/subscribe [::subs/resv])
        i @(re-frame/subscribe [::subs/idx])]
    [:div.columns.is-centered
     [:div.column.is-quarter
      [:h1.title.is-1
       (v->hic i
               res)]]]))
(defmethod res filter []
  (let [res @(re-frame/subscribe [::subs/resv])
        i @(re-frame/subscribe [::subs/idx])]
    [:div.columns.is-centered
     [:div.column.is-quarter
      [:h1.title.is-1
       (v->hic i
               res)]]]))
(defmethod res reduce []
  (let [res @(re-frame/subscribe [::subs/resv])
        ]
    [:div.columns.is-centered
     [:div.column.is-quarter
      [:h1.title.is-1.highlight (str res)]]]))

(defmulti func (fn [] @(re-frame/subscribe [::subs/hof])))

(defmethod func map []
  (let [f @(re-frame/subscribe [::subs/f])
        f-name "inc"
        i @(re-frame/subscribe [::subs/idx])
        v @(re-frame/subscribe [::subs/v])
        el (get v i) ]
    (if (and (<= 0 i) (< i (count v)))
      [:div.columns.is-centered
       [:div.column.is-quarter
        [:h1.title.is-1 [:text "("] [:text f-name] [:text " "] [:text el] [:text ")"]
         [:text " -> "] [:text (f el)]]]]
      [:div.columns.is-centered
       [:div.column.is-quarter
        [:h1.title.is-1 [:text "("] [:text f-name] [:text " "] [:text el] [:text ")"]]]])))

(defmethod func filter []
  (let [f @(re-frame/subscribe [::subs/f])
        f-name "even?"
        i @(re-frame/subscribe [::subs/idx])
        v @(re-frame/subscribe [::subs/v])
        el (get v i) ]
    (if (and (<= 0 i) (< i (count v)))
      [:div.columns.is-centered
       [:div.column.is-quarter
        [:h1.title.is-1 [:text "("] [:text f-name] [:text " "] [:text el] [:text ")"]
         [:text " -> "] [:text (str (f el))]]]]
      [:div.columns.is-centered
       [:div.column.is-quarter
        [:h1.title.is-1 [:text "("] [:text f-name] [:text " "] [:text el] [:text ")"]]]])))

(defmethod func reduce []
  (let [f @(re-frame/subscribe [::subs/f])
        f-name "+"
        i @(re-frame/subscribe [::subs/idx])
        v @(re-frame/subscribe [::subs/v])
        resv @(re-frame/subscribe [::subs/resv])
        el (get v i) ]
    (if (and (<= 0 i) (< i (count v)))
      [:div.columns.is-centered
       [:div.column.is-quarter
        [:h1.title.is-1 [:text "("] [:text f-name] [:text " "] [:text resv] [:text " "][:text el] [:text ")"]
         [:text " -> "] [:text (str (f resv el))]]]]
      [:div.columns.is-centered
       [:div.column.is-quarter
        [:h1.title.is-1 [:text "("] [:text f-name] [:text " "] [:text el] [:text ")"]]]])))

(defn op-btns []
  [:div.columns.is-centered
   [:div.column.is-quarter
    [:button.button {:on-click #(do (re-frame/dispatch [:populate-data])
                                    (re-frame/dispatch [:reset-idx]))}
     "generate data"]
    [:button.button {:on-click #(do (re-frame/dispatch [:reset-idx]) ;; order of events is important. reset res-v before starting. 
                                    (re-frame/dispatch [:start]))}
     "start"]]])

(defn function-selectors []
  (let [hof @(re-frame/subscribe [::subs/hof])
        hofs {map "map" filter "filter"
              reduce "reduce"}]
    [:buttons.has-addons
    (for [f (keys hofs)]
      (if (= f hof)
        [:button.button.is-success.is-selected {:on-click #(do (re-frame/dispatch [:reset-idx])
                                                               (re-frame/dispatch [:change-hof f]))} (get hofs f)]
        [:button.button {:on-click #(do (re-frame/dispatch [:reset-idx])
                                        (re-frame/dispatch [:change-hof f]))} (get hofs f)]))]))

;; (defn code-in []
;; (let [code (r/atom "")]
;;    [:div
;;     [:textarea.textarea
;;      {:placeholder "(map inc [1 2 3 4])"
;;       :content @code
;;       :on-change #(reset! code (-> % .-target .-value))}]
;;     [:button.button {:on-click #(do (re-frame/dispatch [:reset-everything]))} "run"]]))

(defn code-in []
  (let [written-text (r/atom "")]
    (fn []
      [:div
       [:textarea.textarea
        {:placeholder "(map inc [1 2 3 4])"
         :value        @written-text
         :on-change    #(reset! written-text (.. % -target -value))
        ;; :on-key-press (fn [e]
        ;;                 (when (= (.-charCode e) 13)
        ;;      k             (.preventDefault e)
        ;;                   (reset! written-text "")))
         }]
       [:button.button {:on-click #(do (re-frame/dispatch [:reset-everything])
                                       (re-frame/dispatch [:parse-input @written-text])
                                       (reset! written-text "")
                                       )}]])))

(defn main-panel []
  [:div.block]
  [:div.block]
  [:div.container.is-align-content-center
   [function-selectors]
   [:div.block]
   [data-v]
   [:div.block]
   [func]
   [:div.block]
   [res]
   [:div.block]
   [op-btns]
   [:div.block]])
  ;;  [code-in]
   
 