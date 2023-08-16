(ns clojure-htmx-experiment.ui.button 
  (:require
   [clojure-htmx-experiment.app-state :refer [app-state]]))

(defn <button>
  []
  [:button {:hx-post "/clicked" :hx-swap "outerHTML"}
   (if (some-> @app-state :counter (< 1))
     "Click me"
     (str "Clicked "
          (some-> @app-state :counter) " times"))])
