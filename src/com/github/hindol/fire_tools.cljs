(ns com.github.hindol.fire-tools
  (:require [moment]
            [rum.core :as rum]))

(js/console.log moment)
(println (str "Hello there it's "
              (.format (moment) "dddd")))

(rum/defc repeat-label [n text]
  (into
   [:div]
   (for [k (range n)]
     ^{:key k} [:.label text])))

(rum/mount (repeat-label 5 "abc") (.getElementById js/document "app"))
