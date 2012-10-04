(ns cljs-ring-remotes.macros)

(defmacro remote
  [end-point [sym & params] & [destruct & body]]
  (let [func (if destruct
               `(fn ~destruct ~@body)
               nil)]
    `(cljs-ring-remotes.core/remote-callback ~end-point
                                             ~(name sym)
                                             ~(vec params)
                                             ~func)))

(defmacro letrem
  [end-point bindings & body]
  (let [bindings (partition 2 bindings)]
    (reduce
      (fn [prev [destruct func]]
        `(remote ~end-point ~func [~destruct] ~prev))
      `(do ~@body)
      (reverse bindings))))

; Expose here for ease of use.
(defmacro end-point [name path]
  `(cljs-ring-remotes.core/end-point ~name ~path))
