(ns clojure-htmx-experiment.app-state
  (:require
   [mount.core :refer [defstate]]))

(declare app-state)
(defstate app-state
  :start (atom {:counter 0
                :messages []}))

(declare channel-hub)
(defstate channel-hub
  :start (atom {}))


