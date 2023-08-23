(ns clojure-htmx-experiment.core
  (:require
   [clojure.data.json :as json]
 ; [clojure.java.io :as io]
   [clojure-htmx-experiment.app-state :refer [app-state]]
   [clojure-htmx-experiment.ui.app :refer [<app-frame>]]
   [clojure-htmx-experiment.ui.button :refer [<button>]]
   [clojure.core.async :as async]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [hiccup2.core :as h] ; [ring.adapter.jetty :refer [run-jetty]]
   [mount.core :as mount :refer [defstate]]
   [org.httpkit.server :as server :refer [as-channel run-server send!]] ; [ring.core.protocols :refer [StreamableResponseBody]]
   ; [ring-sse-middleware.adapter.http-kit :as adapter]
   ; [ring-sse-middleware.core :as r]
   [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]))

(declare chatroom-messages-ch)
(defstate chatroom-messages-ch
  :start (do (println "creating multi channel")
             (let [ch (async/chan)
                   mult (async/mult ch)]
               [ch mult]))
  :stop (async/close! (first chatroom-messages-ch)))

(defroutes app
  (GET "/" [] (str (h/html (<app-frame>))))
  (GET "/current-time" [] (str (h/html (str (new java.util.Date)))))
  (GET "/chatroom-messages"
    req
    (if-not (:websocket? req)
      (do
        (println "request is not websocket" (keys req))
        (str (h/html "OK")))
      (as-channel
       req
       {:on-receive (fn [_ch message] (println "on-receive:" message))
        :on-close   (fn [_ch status]  (println "on-close:"   status))
        :on-open (fn [resp-ch]
                   (println "on-reponse")
                   (send! resp-ch
                          (json/write-str {:id "messages"
                                           :body ["Hello"]}))
                   #_(let [ch (async/chan)]
                       (println "tapping channel")
                       (async/tap chatroom-messages-ch ch)
                       (println "awaiting message")
                       (let [msg (async/<!! ch)]
                         (println "message received")
                         (println "untapping")
                         (async/untap chatroom-messages-ch ch)
                         (println "sending http response")
                         (send! resp-ch (str (h/html msg))))))})))
  (POST "/clicked"
    []
    (do
      (swap! app-state update-in [:counter] inc)
      (async/go
        (println "PUTTING message")
        (async/>! (first chatroom-messages-ch) (:counter @app-state))
        (println "PUTTING message Done"))
      (str (h/html (<button>)))))
  (route/not-found "<h1>Page not found</h1>"))

; (def site
;   (wrap-defaults app site-defaults))

(declare http-server)
(defstate http-server
  :start (run-server (-> #'app
                         (wrap-defaults api-defaults)
                         #_(r/streaming-middleware
                            adapter/generate-stream
                            {:request-matcher (fn [req]
                                                (let [match? (= (-> req :uri) "/chatroom-messages")]
                                                  (when match? req)))}))
                     {:port 8080 :join? false :async? true})
  :stop (http-server))

(defn -main
  [& _args]
  (mount/start))

(comment
  (prn site-defaults)
  (prn :hello)
  (prn (async/close! chatroom-messages-ch))
  (do
    (mount/stop)
    (mount/start)))
