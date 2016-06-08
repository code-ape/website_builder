(ns website-builder.core
  (:require [stasis.core :as stasis]
            [markdown.core :as md]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [markdown.core :as md]
            [net.cgrand.enlive-html :as enlive])
  (:use [clojure.set :only (subset?)]
        [ring.adapter.jetty :only (run-jetty)]
        [ring.middleware.file :only (wrap-file)]
        [ring.middleware.content-type :only (wrap-content-type)]
        [ring.middleware.not-modified :only (wrap-not-modified)])
  (:gen-class))

(def pages-dir-name "pages")
(def pages-dir (clojure.java.io/file pages-dir-name))
(def output-dir "output")

(def handler
  (-> (fn [r] nil)
    (wrap-file output-dir) 
    (wrap-content-type)))

(defonce server (run-jetty #'handler {:port 3000 :join? false}))


(defn contains-page-files
  "Takes a java dir and returns whether it contains meta.edn and page.md"
  [j-dir]
  (let [files (->> (.listFiles j-dir)
                   (filter #(.isFile %))
                   (map #(.getName %))
                   (set))]
    (if (subset? #{"meta.edn" "page.md"} files)
      files
      false)))

(defn data-from-dir
  "returns data structure of meta.edn and page.md in given dir"
  [dir-str]
  (let [meta-path (string/join "/" [dir-str "meta.edn"])
        page-path (string/join "/" [dir-str "page.md"])]
    {:data (edn/read-string (slurp meta-path))
     :page (slurp page-path)}))

(defn pages
  "Returns seq of map with page data and markdown text"
  []
  (let [files (file-seq pages-dir)]
    (->> files
         (filter #(.isDirectory %))
         (filter contains-page-files)
         (map #(.getPath %))
         (map data-from-dir))))


(defn build-path
  [{:keys [data]}]
  (-> data
      :name 
      (string/replace #"[^\p{L}\p{Nd}]+" "_") 
      (string/lower-case)
      (str ".html")))

(defn build-html
  [{:keys [data page]}]
  (md/md-to-html-string page))

(defn build-page
  [page-data]
  {:path (build-path page-data)
   :html (build-html page-data)})

(defn save-page
  [{:keys [html path]}]
  (let [full-path (string/join "/" [output-dir path])]
    (spit full-path html)))

(defn save-website
  []
  (let [p (pages)]
    (map save-page (map build-page p))))

(defn enlive-markdown
  []
  (let [p (pages)]
    (->> p (map build-page) (map #(enlive/html-snippet (:html %)))))) 


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
