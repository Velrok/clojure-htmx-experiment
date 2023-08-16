(ns clojure-htmx-experiment.core
  (:require
   [clojure.core.async :as async]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [ring.core.protocols :refer [StreamableResponseBody]]
   [hiccup2.core :as h] ; [ring.adapter.jetty :refer [run-jetty]]
   [mount.core :as mount :refer [defstate]]
   [org.httpkit.server :refer [run-server]]
   [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]))

; taken from https://www.lucagrulla.com/posts/server-sent-events-with-ring-and-compojure/
(extend-type clojure.core.async.impl.channels.ManyToManyChannel
  StreamableResponseBody
  (write-body-to-stream [channel _response output-stream]
    (async/go (with-open [writer (io/writer output-stream)]
                (async/loop []
                  (when-let [msg (async/<! channel)]
                    (doto writer (.write msg) (.flush))
                    ; looks like kondo does understand that async/loop is a recur target
                    #_{:clj-kondo/ignore [:invalid-arity]}
                    (recur)))))))

(def stream-response
  (partial
   assoc
   {:status 200,
    :headers {"Content-Type" "text/event-stream"}}
   :body))

(def EOL "\n")

(defn stream-msg [payload]
  (str "data:" (json/write-str payload) EOL EOL))

(declare app-state)
(defstate app-state
  :start (atom {:counter 0
                :messages []}))

(defn <button>
  []
  [:button {:hx-post "/clicked" :hx-swap "outerHTML"}
   (if (some-> @app-state :counter (< 1))
     "Click me"
     (str "Clicked "
          (some-> @app-state :counter) " times"))])

(defn <chat-room>
  []
  [:div {:hx-ext :sse :sse-connect "/chatroom-messages" :sse-swap "message"}
   "Loading messages..."])

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

(declare chatroom-messages-ch)
(defstate chatroom-messages-ch
  :start (async/chan)
  :stop (async/close! @chatroom-messages-ch))

(defroutes app
  (GET "/" [] (str (h/html app-frame)))
  (GET "/chatroom-messages"
       [_req res _raise]
    ; []
    ; "stream goes here"
       (res (stream-response @chatroom-messages-ch))
    #_(async/go (async/>! chatroom-messages-ch (stream-msg {:val 42}))
                (async/<! (async/timeout 1000))
                (async/>! chatroom-messages-ch (stream-msg {:val 100}))
                (async/close! chatroom-messages-ch)))
  (POST "/clicked"
    []
    (do
      (swap! app-state update-in [:counter] inc)
      (async/>! @chatroom-messages-ch
                (stream-msg
                 (str "Received click no "
                      (some-> @app-state :counter)))))
    (route/not-found "<h1>Page not found</h1>")))

; (def site
;   (wrap-defaults app site-defaults))

(declare http-server)
(defstate http-server
  :start (run-server (-> #'app (wrap-defaults api-defaults))
                     {:port 8080 :join? false :async? true})
  :stop (http-server))

(defn -main
  [& _args]
  (mount/start))

(comment
  (prn site-defaults)
  (prn :hello)
  (do
    (mount/stop)
    (mount/start)))
