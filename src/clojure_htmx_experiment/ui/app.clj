(ns clojure-htmx-experiment.ui.app
  (:require
   [clojure-htmx-experiment.ui.button :refer [<button>]]
   [clojure-htmx-experiment.ui.chat-room :refer [<chat-room>]]))

(def app-frame
  [:html
   [:head]
   [:body
    [:script {:src "https://unpkg.com/htmx.org@1.9.4"}]
    [:script {:src "https://unpkg.com/htmx.org/dist/ext/sse.js"}]
    [:h1 "Hello world 2"]
    (<button>)

    [:h1 "Chat room"]
    (<chat-room>)]])
