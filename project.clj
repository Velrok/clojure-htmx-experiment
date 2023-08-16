(defproject clojure-htmx-experiment "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :main clojure-htmx-experiment.core
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [compojure "1.7.0"]
                 [ring "1.10.0"]
                 [hiccup "2.0.0-RC1"]
                 [org.clojure/core.async "1.6.681"]
                 [mount "0.1.17"]
                 [http-kit/http-kit "2.7.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-defaults "0.3.4"]]
  :repl-options {:init-ns clojure-htmx-experiment.core})
