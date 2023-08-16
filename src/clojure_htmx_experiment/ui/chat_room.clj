(ns clojure-htmx-experiment.ui.chat-room)

(defn <chat-room>
  []
  [:div {:hx-ext :sse :sse-connect "/chatroom-messages" :sse-swap "message"}
   "Loading messages..."])
