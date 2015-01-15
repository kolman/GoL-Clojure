(defproject joyful-game "0.1.0-SNAPSHOT"
  :description "Conway's Game of Life in Clojure, illustrated by Gorilla"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot gorilla-test.core
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[lein-gorilla "0.3.4"]])
