# redantd
让reagent使用Ant Design更简单。

## 依赖
* [shadow-cljs](http://shadow-cljs.org/)
* [reagent](https://github.com/reagent-project/reagent)
* [Ant Design](https://ant.design/index-cn)

## 使用
[![Clojars Project](https://img.shields.io/clojars/v/redantd.svg)](https://clojars.org/redantd)

## 使用前后代码比较

### 使用前
```cljs
(require '["antd" :as ant])

;;子组件需要额外声明
(def SubMenu (.-SubMenu ant/Menu))
(def ItemGroup (.-ItemGroup ant/Menu))
(defn Item (.-Item ant/Menu))

[:> ant/Menu
   {:onClick (fn [itm]
               (let [clj-itm (js->clj itm :keywordize-keys true)]  ;;函数参数需要通过js->clj数据转换
                 (prn clj-itm)))}
   [:> SubMenu
    {:key "sub1"
     :title (r/as-element  ;;需要通过reagent.core/as-element转换
             [:span
              [:> ant/Icon {:type "mail"}]
              [:span "Navigation One"]])}
    [:> ItemGroup
    {:title "Item 1"}
     [:> Item
      {:key "1"}
      "Option 1"]
     [:> Item
      {:key "2"}
      "Option 2"]]]]
```
### 使用后
```cljs
(require '[redant.core :as ant])

;;无需声明子组件
(ant/menu
   {:onClick (fn [itm] (prn itm))}  ;;无需额外的通过js->clj数据转换
   (ant/menu-sub-menu
    {:key "sub1"
    :title [:span                   ;;无需reagent.core/as-element转换
             (ant/icon {:type "mail"})
             [:span "Navigation One"]]}
    (ant/menu-item-group
     {:title "Item 1"}
     (ant/menu-item {:key "1"} "Option 1")
     (ant/menu-item {:key "2"} "Option 2"))))
```
