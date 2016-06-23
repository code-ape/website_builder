(ns website.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [garden.core :as garden :refer [css]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)


(defn load-data []
    (go (let [m (<! (http/get "/edn/meta.edn"))
              c (<! (http/get "/edn/page2.edn"))]
          (println "Finished load-data!")
          (println "m:" m)
          (println "c:" c)
          (println "(-> c :body):" (-> c :body))
          (println "(-> c :body :content):" (-> c :body :content))
          (println "(get-in c [:body :content]):" (get-in c [:body :content]))
          {:meta (:body m)
           :content (get-in c [:body :content])})))


(def dummy-data {:meta {:name "My post!"}
                 :content "Some content for you, and you, and you too...."})


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
                        (clojure.string/join " ")
                        (#(str % " ")))
        rest-words (last word-split)]
    (style
    [:div
     [:title title]
     [:content [:accent-font first-words] rest-words]])))


(defn article-page
  [data]
  (let [title (get-in data [:meta :name])
        content (get-in data [:content])]
    (style
      [:container
       [:row
        [:sub-half
         [:fixed-center
          [:text-right
           [:large-font
            [:accent-font "Creative" [:accent-color "8"] [:br] "Labs"]]]]]
        [:main-half [article title title title content]]]])))

(defn master-loader
  []
  (let [d (reagent/atom dummy-data)]
    (println "A")
    (go
      (reset! d (<! (load-data)))
      (println @d))
    (println "B")
    #(article-page @d)))

(defn init []
  (do
    (update-page-css custom-css)
    (reagent/render-component
      [master-loader]
      (.getElementById js/document "container"))))
