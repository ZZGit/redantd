(ns redantd.macros
  (:require
   [clojure.string :as s]
   [redantd.antd :as antd]))

(defn module-name->kebab-case
  [input]
  (->> (re-seq #"\w[a-z0-9]*" input)
       (map s/lower-case)
       (s/join "-")))

(def get-symbol-name (comp symbol module-name->kebab-case))

(defn get-module-path [module-name]
  (s/split module-name #"\."))

(defn defn-fn [fn-name]
  (let [fn-name (name fn-name)]
    `(defn ~(get-symbol-name fn-name) [& args#]
       (apply util/call-func ~'ant ~fn-name args#))))

(defn defn-form-funcs [method-name]
  (let [method-name (name method-name)]
    `(defn ~(get-symbol-name method-name) [form# & args#]
       (apply util/call-js-func ((keyword ~method-name) form#) args#))))

(defn defn-reagent-component [component]
  (let [component (name component)
        component-key (keyword (module-name->kebab-case component))
        component-name (get-symbol-name component)
        module-path (get-module-path component)]
    `(defn ~component-name [& args#]
       (if (map? (first args#))
         (into
          [(r/adapt-react-class
            (apply goog.object/getValueByKeys
                   ~'ant
                   ~module-path))
           (util/process-props
            (first args#))]
          (rest args#))
         (into
          [(r/adapt-react-class
            (apply goog.object/getValueByKeys
                   ~'ant
                   ~module-path))]
          args#)))))

(defmacro export-funcs []
  `(do ~@(map defn-fn antd/funcs)))

(defmacro export-form-funcs []
  `(do ~@(map defn-form-funcs antd/form-funcs)))

(defmacro export-reagent-components []
  `(do ~@(map (fn [c] (defn-reagent-component c)) antd/components)))
