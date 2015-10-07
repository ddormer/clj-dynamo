(defproject clj-dynamo "0.1.0"
  :description "IRC bot that announces Bitbucket and Trello web hooks."
  :url "http://github.com/ddormer/clj-dynamo"
  :license {:name "Apache License 2.0"
            :url "http://opensource.org/licenses/Apache-2.0"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [cheshire "5.5.0"]
                 [compojure "1.3.4"]
                 [org.clojure/tools.cli "0.3.1"]
                 [irclj "0.5.0-alpha4"]
                 [http-kit "2.1.18"]]
  :main ^:skip-aot clj-dynamo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
