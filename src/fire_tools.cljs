(ns fire-tools
  (:require [moment]))

(js/console.log moment)
(println (str "Hello there it's "
              (.format (moment) "dddd")))
