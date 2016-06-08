(ns website.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [garden.core :as garden :refer [css]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(defonce post-data (atom {:meta nil :content nil}))

(defn load-data []
  (do
    (go (let [m (<! (http/get "/edn/meta.edn"))]
          ;;(println "Loaded /edn/meta.edn")
          ;;(println m)
          (swap! post-data assoc :meta (m :body))))
    (go (let [c (<! (http/get "/edn/page2.edn"))]
          ;;(println "Loaded /edn/page2.edn")
          ;;(println c)
          ;;(println (-> c :body :content))
          (swap! post-data assoc :content (-> c :body :content))))))

(defn generate-and-inject-style-tag
  "Injects a style tag with the id 'injected-css' into the page's head tag
  Returns generated style tag"
  []
  (let [ page-head (.-head js/document)
        style-tag (.createElement js/document "style")]
    (.setAttribute style-tag "id" "injected-css")
    (.appendChild page-head style-tag)))

(defn update-page-css
  "Updates #injected-css with provided argument (should be some CSS string
  -- e.g. output from garden's css fn) If page does not have #injected-css then
  will create it via call to generate-and-inject-style-tag"
  [input-css]
  (let [style-tag-selector "#injected-css"
        style-tag-query (.querySelector js/document style-tag-selector)
        style-tag (if (nil? style-tag-query)
                    (generate-and-inject-style-tag)
                    style-tag-query)]
    (do
      ;;(println "New css: " input-css)
      (aset style-tag "innerHTML" input-css))))


; Usage 
; (Assumes you've required garden in your namespace)
; (Optionally throw this in your boot-reload / figwheel reload CB)
(def custom-css
  (garden/css [:h1 {:font-family "Playfair Display"}]
              [:p  {:font-family "Georgia"}]
              [:.title {:font-family "Playfair Display"}]
              [:.large-font {:font-size "40px"}]
              [:.normal-font {:font-size "18px"}]
              [:.accent-color {:color "#FF6F00"}]
              [:.accent-font  {:font-family "Modern Sans"}]
              [:.reader-spacing {:line-height "150%" :margin-top "2em"}]
              [:.fixed-center {:position "fixed" :top "30%"}]))

(def style-map
  {:title [:p {:class [:title :large-font :reader-spacing]}]
   :content [:p {:class [:normal-font :reader-spacing]}]
   :large-font [:h1 {:class :large-font}]
   :accent-color [:span {:class :accent-color}]
   :accent-font [:span {:class :accent-font}]
   :container [:div {:class :container}]
   :row [:div {:class :row}]
   :main-half [:div {:class [:col-xs-12 :col-sm-8 :col-md-9 :col-lg-8]}]
   :sub-half [:div {:class [:hidden-xs :col-sm-4 :col-md-3]}]
   :fixed-center [:div {:class :fixed-center}]
   :text-right [:div {:class :text-right}]})

(defn ->class
  [class-thing]
  (if (sequential? class-thing)
    (do
      (->> class-thing (map name) (clojure.string/join " ")))
    class-thing))

(defn style [components]
  (->> components
       (map
         (fn [c]
           (cond
             (sequential? c)
             [(style c)]

             (map? c)
             [(update c :class ->class)]

             (contains? style-map c)
             (style (style-map c))
             
             :else
             [c])))
       (apply concat)
       (into [])))

(defn article [title author date content]
  (let [word-split (clojure.string/split content #" " 5)
        first-words (->> word-split 
                        (take 4)
                        (clojure.string/join " "))
        rest-words (str " " (last word-split))]
    (style
    [:div
     [:title title]
     [:content [:accent-font first-words] rest-words]])))


(defn article-page []
  (let [title (get-in @post-data [:meta :name])
        content (get-in @post-data [:content])]
    (style
      [:container
       [:row
        [:sub-half
         [:fixed-center
          [:text-right
           [:large-font
            [:accent-font "Creative" [:accent-color "8"] [:br] "Labs"]]]]]
        [:main-half [article title title title content]]]])))


(defn init []
  (do
    (load-data)
    (update-page-css custom-css)
    (reagent/render-component
      [article-page]
      (.getElementById js/document "container"))))
