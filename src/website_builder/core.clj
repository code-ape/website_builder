(ns website-builder.core
  (:require [stasis.core :as stasis]
            [markdown.core :as md]
            [clojure.string :as string]
            [clojure.edn :as edn])
  (:use [clojure.set :only (subset?)])
  (:gen-class))

(def pages-dir-name "pages")
(def pages-dir (clojure.java.io/file pages-dir-name))


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


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
