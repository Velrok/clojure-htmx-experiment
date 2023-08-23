(ns clojure-htmx-experiment.ui.server-time)

(defn <server-time>
  []
  [:div {:hx-get "/current-time" :hx-trigger "every 2s"}
   "Loading ..."])
