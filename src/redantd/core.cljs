(ns redantd.core
  (:refer-clojure :exclude [list empty comment])
  (:require
   ["antd" :as ant]
   ["antd/es/locale/en_US" :default en-us]
   ["antd/es/locale/zh_CN" :default zh-cn]
   [reagent.core :as r]
   [redantd.util :as util])
  (:require-macros
   [redantd.macros :refer [export-funcs
                           export-form-funcs
                           export-reagent-components]]))

(def locale-zh_CN zh-cn)
(def locale-en_US en-us)

(export-funcs)
(export-form-funcs)
(export-reagent-components)

(defn set-icon-two-tone-color [color]
  (.setTwoToneColor ant/Icon color))

(defn get-icon-two-tone-color []
  (.getTwoToneColor ant/Icon))

(defn create-from-iconfont-cn [props]
  (.createFromIconfontCN ant/Icon (clj->js props)))


(defn get-form
  []
  (-> (r/current-component)
      (r/props)
      (js->clj :keywordize-keys true)
      (:form)))

(defn create-form
  [form & {:keys [options props] :or {options {} props {}}}]
  (r/create-element
   (((goog.object/getValueByKeys ant "Form" "create") (clj->js (util/map-keys->camel-case options)))
    (r/reactify-component
     (fn [props]
       (form (assoc props :form (get-form))))))
   (clj->js props)))

(defn get-field-decorator
  ([id field] (get-field-decorator id {} field))
  ([id options field] (get-field-decorator id (get-form) options field))
  ([id form options field]
   (let [form (get-form)
         field-decorator (:getFieldDecorator form)
         params (clj->js (util/map-keys->camel-case options))]
     ((field-decorator id params)
      (if (= (type field) cljs.core/PersistentVector)
        (let [[c & r] field]
          (if (util/function-form? c)
            (r/create-element
             (r/reactify-component c))
            (r/as-element field)))
        (r/as-element field))))))

(defn validate-fields
  [form callback]
  ((:validateFields form)
   (fn [errors values]
     (callback (js->clj errors :keywordize-keys true)
               (js->clj values :keywordize-keys true)))))

(defn validate-fields-and-scroll
  [form callback]
  ((:validateFieldsAndScroll form)
   (fn [errors values]
     (callback (js->clj errors :keywordize-keys true)
               (js->clj values :keywordize-keys true)))))
