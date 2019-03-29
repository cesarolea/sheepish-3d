(defproject sheepish-3d "0.1.0-SNAPSHOT"
  :description "Sheepish 3D"
  :url "https://blog.cesarolea.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :pedantic? :abort

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [mount "0.1.13"]
                 [quil "2.8.0"]]

  :main ^:skip-aot sheepish-3d.core
  :target-path "target/%s"
  :source-paths ["src/clj" "src/cljs" "src/cljs/sheepish_3d"]
  :resource-paths ["resources"]

  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "fig-dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig-prod" ["trampoline" "run" "-m" "figwheel.main" "-O" "advanced" "-bo" "prod"]}

  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[com.bhauman/figwheel-main "0.2.0"
                                   :exclusions [args4j commons-codec]]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"
                                   :exclusions [args4j]]
                                  [cider/piggieback "0.4.0"
                                   :exclusions [args4j]]
                                  [org.clojure/clojurescript "1.10.439"
                                   :exclusions [com.google.errorprone/error_prone_annotations
                                                com.google.code.findbugs/jsr305]]]
                   :source-paths ["env/dev/clj"]
                   :repl-options {:init-ns user
                                  :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
