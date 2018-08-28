(ns kinese.logging)

(println "foo")

(defmacro log [& args]
  `(.log js/console ~@args))

(defmacro info [& args]
  `(.info js/console ~@args))
