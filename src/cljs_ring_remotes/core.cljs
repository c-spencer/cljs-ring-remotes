(ns cljs-ring-remotes.core
  (:require [goog.net.XhrIo :as xhr]
            [clojure.string :as string]
            [cljs.reader :as reader]
            [cljs-ring-remotes.util :as util]
            [goog.events :as events]
            [goog.Uri.QueryData :as query-data]
            [goog.structs :as structs]))

; Store map of end-points
(def end-points (atom {}))
(defn end-point [name path] (swap! end-points assoc name path))

(defn ->method [m]
  (string/upper-case (name m)))

(defn parse-route [route]
  (cond
    (string? route) ["GET" route]
    (vector? route) (let [[m u] route]
                      [(->method m) u])
    :else ["GET" route]))

(defn ->data [d]
  (let [cur (util/clj->js d)
        query (query-data/createFromMap (structs/Map. cur))]
    (str query)))

(defn ->callback [callback]
  (when callback
    (fn [req]
      (let [data (. req (getResponseText))]
        (callback data)))))

(defn xhr [route content callback & [opts]]
  (let [req (new goog.net.XhrIo)
        [method uri] (parse-route route)
        data (->data content)
        callback (->callback callback)]
    (when callback
      (events/listen req goog.net.EventType/COMPLETE #(callback req)))
    (. req (send uri method data (when opts (util/clj->js opts))))))

(defn remote-callback [end-point remote params callback]
  (xhr  [:post (get @end-points end-point)]
        {:method remote
         :args (pr-str params)}
        (when callback
          (fn [data]
            (let [data (if (= data "") "nil" data)]
              (callback (reader/read-string data)))))))
