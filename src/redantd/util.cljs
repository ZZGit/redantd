(ns redantd.util
  (:require
   [reagent.core :as r]
   [clojure.string :as s]
   [clojure.walk :as w]
   [clojure.set :refer [rename-keys]]
   [goog.object :refer [getValueByKeys]]))


(defn kebab-case->camel-case
  "Converts from kebab case to camel case, eg: on-click to onClick"
  [input]
  (let [words (s/split input #"-")
        capitalize (->> (rest words)
                        (map #(apply str (s/upper-case (first %)) (rest %))))]
    (apply str (first words) capitalize)))

(defn kebab-case-key->camel-case-key
  [input]
  (-> input (name) (kebab-case->camel-case) (keyword)))

(defn map-form?
  "if map form eg. {:a 1 :b 2}"
  [value]
  (= (type value) cljs.core/PersistentArrayMap))

(def is-function (or goog/isFunction goog.functions/isFunction))

(defn- hiccup-form?
  "if hiccup form eg: [:div] [component]"
  [value]
  (and
   (= (type value) cljs.core/PersistentVector)
   (not (map-form? (first value)))
   (or (is-function (first value))
       (goog/isObject (first value))
       (= (type (first value)) cljs.core/Keyword))))

(defn- vector-map-form?
  "if vector map form eg. [{:a 1} {:b 2}]"
  [value]
  (and
   (= (type value) cljs.core/PersistentVector)
   (map-form? (first value))))

(defn- vector-hiccup-form?
  "if vector hiccup form eg. [[:div] [:a]]"
  [value]
  (and
   (= (type value) cljs.core/PersistentVector)
   (hiccup-form? (first value))))

(defn function-form?
  "if function form"
  [value]
  (is-function value))

(defn recur-process-prop [[k v]]
  (let [value (cond
                (hiccup-form? v) (r/as-element v)
                (map-form? v) (into {} (map recur-process-prop v))
                (vector-map-form? v) (mapv #(into {} (map recur-process-prop %))  v)
                (vector-hiccup-form? v) (map r/as-element v)
                (function-form? v) (fn [& args]
                                     (if (= :render k)
                                       (r/as-element
                                        (apply v (map #(js->clj % :keywordize-keys true) args)))
                                       (apply v (map #(js->clj % :keywordize-keys true) args))))
                :else v)]
    [k value]))

(defn process-props [props]
  (if (map-form? props)
    (into {} (map recur-process-prop props))
    props))

(defn map-keys->camel-case
  "Stringifys all the keys of a cljs hashmap and converts them
   from kebab case to camel case. If :html-props option is specified,
   then rename the html properties values to their dom equivalent
   before conversion"
  [data & {:keys [html-props]}]
  (let [convert-to-camel (fn [[key value]]
                           [(kebab-case->camel-case (name key)) value])]
    (w/postwalk (fn [x]
                  (if (map? x)
                    (let [new-map (if html-props
                                    (rename-keys x {:class :className :for :htmlFor})
                                    x)]
                      (into {} (map convert-to-camel new-map)))
                    x))
                data)))

(defn get-module-path [module-name]
  (s/split module-name #"\."))

(defn get-prop
  "Retreives the value of the module's property"
  [antd module-name prop]
  (when prop
    (apply getValueByKeys antd (conj (get-module-path module-name) (name prop)))))

(defn call-js-func
  "Calls a javascript function, converting the keys for any arguments
   that are hashmaps from kebab case to camel case

   * func - the native javascript to be called"
  [func & args]
  (js->clj
   (apply func (clj->js (map
                         #(-> %
                              (map-keys->camel-case)
                              (process-props)) args)))
   :keywordize-keys true))

(defn call-func
  "Calls the ant module function"
  [antd module-name & args]
  (let [path (get-module-path module-name)
        func (apply getValueByKeys antd path)]
    (apply call-js-func func args)))
