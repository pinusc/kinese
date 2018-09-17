(def figwheel-version "0.5.15")
(def environ-version "1.1.0")
(defproject kinese "0.1.0-ALPHA"
  :description "A reading helper with interactive interlinear dictionary for different languages"
  :url "http://example.com/FIXME"
  :license {:name "GNU General Public License Version 3"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories {"local" {:url "file:lib" :username "" :password ""}}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring-server "0.4.0"]
                 [reagent "0.7.0"]
                 [reagent-utils "0.2.1"]
                 [ring "1.6.1"]
                 [ring/ring-defaults "0.3.0"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [yogthos/config "0.8"]
                 [org.clojure/clojurescript "1.10.339" :scope "provided"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.0" :exclusions [org.clojure/tools.reader]]
                 [org.clojure/tools.trace "0.7.9"]
                 [cljs-ajax "0.6.0"]
                 [ring-transit "0.1.6"]
                 [fnlp-core "2.1"]
                 [ring-json-response "0.2.0"]
                 [net.sf.trove4j/trove4j "3.0.3"]
                 [commons-cli "1.2"]]

  :plugins [[lein-expand-resource-paths "0.0.1"] ; globbing for fnlp
            [lein-environ ~environ-version]
            [lein-cljsbuild "1.1.6"]
            [yogthos/lein-sass "0.1.4"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler kinese.handler/app
         :uberwar-name "kinese.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "kinese.jar"

  :main kinese.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  ;; declaring resources/fnlp-core-2.1.jar creates a dir when the file is not found
  ;; which breaks the build process
  :resource-paths ["resources" "target/cljsbuild"] 

  :sass {:source "src/scss" :target "resources/public/css"}

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild
  {:builds {:min {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
                  :compiler {:output-to "target/cljsbuild/public/js/app.js"
                             :output-dir "target/uberjar"
                             :optimizations :advanced
                             :pretty-print  false}}
            :app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                  :figwheel {:on-jsload "kinese.core/mount-root"}
                  :compiler {:main "kinese.dev"
                             :asset-path "/js/out"
                             :output-to "target/cljsbuild/public/js/app.js"
                             :output-dir "target/cljsbuild/public/js/out"
                             :source-map true
                             :optimizations :none
                             :preloads [devtools.preload]}}}}

  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   ;; :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
   :css-dirs ["resources/public/css"]
   :ring-handler kinese.handler/app}

  :profiles {:dev {:repl-options {:init-ns kinese.repl
                                  :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

                   :dependencies [
                                  [binaryage/devtools "0.9.10"]
                                  [binaryage/dirac "RELEASE"]
                                  [ring/ring-mock "0.3.1"]
                                  [ring/ring-devel "1.6.1"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar ~figwheel-version]
                                  [figwheel ~figwheel-version]
                                  [environ ~environ-version]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [pjstadig/humane-test-output "0.8.2"]
                                  [cider/piggieback "0.3.8"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel ~figwheel-version]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
