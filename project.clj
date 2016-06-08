(defproject website_builder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [stasis "2.3.0"]
                 [markdown-clj "0.9.89"]
                 [ring/ring-jetty-adapter"1.5.0-RC1"]
                 [ring/ring-core "1.5.0-RC1"]
                 [enlive "1.1.6"]]
  :main ^:skip-aot website-builder.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
