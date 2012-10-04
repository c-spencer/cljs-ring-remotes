# cljs-ring-remotes

A simple ring handler for exposing functions to a cljs client, mostly an extraction and generalisation of fetch: https://github.com/ibdknox/fetch

Reasons this library exists:

- No dependencies, the remoter is just a function that functions as a ring handler function.
- Can expose multiple remote points at different urls, allowing authorisation and modularisation.

## Installation

This is experimental and the api could change. There are likely a few bugs or bad behaviours to tweak.

```
[cljs-ring-remotes "0.1.7"]
```

### Example use

Server-side

```clojure
(ns cljs-ring-remotes.example
  (:require [cljs-ring-remotes.remoter :as remoter]))

; defines a new remoter
(def fetch-point (remoter/create-remoter))

; Exposes a function at a given method point on the remoter.
(remoter/add-remote fetch-point :silly-string #(str "is silly"))

; Defines a new function and exposes it
(remoter/defremote fetch-point sillier-string [person] (str "is even more silly than " person))

; To use in ring, just use as a normal middleware function. Note, it's a dead-end for simplicity, so
; encapsulate it inside a compojure route or similar, like so:

(defroutes my-routes
  (POST "/end-points/_fetch" [] fetch-point))

; It extracts from the ring `params`, expecting `method` and `args`. `args` should be a readable
; sequence of arguments.

{:params {:method "silly-string"
          :args "[]"}}

{:params {:method "sillier-string"
          :args "[\"everyone\"]"}}

```

### Client-side

Use just like fetch, but with an additional parameter to specify which remoter to use.

```clojure
(ns cljs-ring-remotes.client-example
  (:require [cljs-ring-remotes.core :as remotes])
  (:require-macros [cljs-ring-remotes.macros :as r]))

; Define our end-point
(r/end-point :main "/end-points/_fetch")

(r/remote :main (silly-string) [s]
  (js/alert s))

(r/letrem :main [a (silly-string)
                 b (sillier-string "everyone")]
  (js/alert (str a " and " b)))
```

### License

Eclipse Public License, just like Clojure.
