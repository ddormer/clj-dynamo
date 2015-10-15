(ns clj-dynamo.core
  (:gen-class)
  (:use ring.adapter.jetty)
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :refer :all]
            [irclj.core :as irclj]
            [irclj.events :as events]
            [clj-dynamo.trello :refer [trello-handler]]
            [clj-dynamo.bitbucket :refer [handle-bitbucket]]
            [clj-dynamo.shortener :refer [shorten-url]]
            [clj-dynamo.irc :as irc]
            [clj-dynamo.plugin :refer [init-plugins]]))


(defn getConfig
  [path]
  (json/parse-string (slurp path) true))


(defn dynamo-router
  [config send-irc-message]
  (routes
   (HEAD "/trello" [] "")
   (POST "/trello" [] (partial trello-handler
                               send-irc-message
                               (partial shorten-url
                                        (get-in config [:bitly :token]))
                               (get-in config [:trello :key])
                               (get-in config [:trello :callback-url])))
   (POST "/bitbucket" [] (partial handle-bitbucket
                                  send-irc-message
                                  (partial shorten-url
                                           (get-in config [:bitly :token]))))))


(def cli-options
  [["-c" "--config PATH" "Path to the JSON config."]
   ["-h" "--help"]])


(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))


(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn usage [summary]
  (->> ["Usage: clj-dynamo [options]"
        ""
        "Options:"
        summary]
       (string/join \newline)))


(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))

    (let [config (getConfig (:config options))
          shorten-url-configured (partial shorten-url (get-in config [:bitly :token]))
          bitbucket-options (:bitbucket config)
          send-irc-message (irc/setup-irc shorten-url-configured config)]

      (init-plugins shorten-url-configured bitbucket-options)
      {:web-server (run-jetty
                    (dynamo-router config (partial send-irc-message (get-in config [:irc :channel])))
                    {:port (get-in config [:web :port]) :join? false})})))
