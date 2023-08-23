(ns clojure-htmx-experiment.ui.chat-room)

(defn <chat-room>
  []
  ;; https://htmx.org/extensions/web-sockets/
  [:div {:hx-ext :ws :ws-connect "/chatroom-messages"}
   [:div#messages "Loading messages..."]])
