(ns cljs-ring-remotes.remoter)

(defn safe-read [s]
  (binding [*read-eval* false]
    (read-string s)))

(defprotocol PRemoter
  (add-remote [_ name fn]))

(deftype Remoter [fns]
  clojure.lang.IFn
    ; Invoke with a ring handler, return a new handler.
    ; Note this handler is a dead-end and will ALWAYS attempt to run and return given
    ; a request. Other methods, such as compojure contexts or routes should be used to
    ; run this handler selectively.
    ;
    ; Exceptions are left to bubble up to be caught by an outer ring handler.
    (invoke [this handler]
      (fn [{{:keys [method args]} :params}]
        (let [result (this (keyword method) (safe-read args))]
          { :status 200
            :headers {"Content-type" "application/clojure; charset=utf8"}
            :body (pr-str result) })))

    ; Invoke given a name and arguments
    (invoke [_ method args] (apply (get @fns method) args))

    (applyTo [this args] (clojure.lang.AFn/applyToHelper this args))
  PRemoter
    ; Add a function to dispatch to
    (add-remote [_ name fn] (swap! fns assoc name fn)))

; Defines a function and exposes it as a remote on the remoter
(defmacro defremote [remoter & [name :as body]]
  `(do
    (defn ~@body)
    (add-remote ~remoter (keyword '~name) ~name)))

(defn create-remoter []
  (Remoter. (atom {})))
