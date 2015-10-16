(ns clj-dynamo.plugin
  (:require [clj-dynamo.shortener :refer [shorten-url]]
            [clj-dynamo.bitbucket :refer [open-pullrequests]]))


(defprotocol Plugin
  (call [this] [this arguments] "The plugin logic.")
  (usage [this] "Returns a string explaining the plugin's command usage."))


(defrecord Echo [command]
  Plugin
  (call [this arguments] (first arguments))
  (call [this] (usage this))
  (usage [this] (str command " <text>")))


(defrecord BB-Open-Prs [command shorten-url bitbucket-options]
  Plugin
  (call [this arguments] (open-pullrequests shorten-url (first arguments) bitbucket-options))
  (call [this] (usage this))
  (usage [this] (str command " <username/repository>")))


(defn validate-plugin
  "Verify each value in the map satisfies the Plugin interface."
  [state]
  (every? (partial satisfies? Plugin) (vals state)))


(def registry (atom {} :validator validate-plugin))


(defn register-plugin
  [plugin]
  (swap! registry assoc (keyword (:command plugin)) plugin))


(defn call-plugin
  [command & args]
  (let [plugin ((keyword command) @registry)]
    (if-not (nil? plugin)
      (if (nil? args)
        (call plugin)
        (call plugin args))
      (format "Plugin '%s' not found." command))))


(defn init-plugins
  [shorten-url bitbucket-options]
  (do
    (register-plugin (BB-Open-Prs. "prs" shorten-url bitbucket-options)))
    (register-plugin (->Echo "echo")))
