(ns clj-dynamo.irc
  (:require [clojure.core.async :refer [go-loop chan <! >! <!! >!!]]
            [irclj.core :as irclj]
            [irclj.events :as events]
            [clj-dynamo.plugin :refer [call-plugin]]))


(defn- handle-command
  [command text nick shorten-url send-message]
  (when (= command "PRIVMSG")
    (let [[said string] (clojure.string/split text #" " 2)]
      (when (.startsWith said "!")
        (try
            (let [message (call-plugin (.substring said 1) string)]
              (when-not (nil? message)
                (send-message message)))
            (catch Exception e (send-message (.getMessage e))))))))


(defn send-irc-message
  [irc-connection target text]
  (when-not (clojure.string/blank? text)
    (irclj/message irc-connection target text)))


(defn say!
  [outgoing-messages irc-channel message]
  (>!! outgoing-messages {:channel irc-channel :message message}))


(defn handle-incoming [channel shorten-url send-message]
  (go-loop []
    (let [{text :text
           target :target
           nick :nick
           host :host
           command :command} (<! channel)]
      (handle-command command text nick shorten-url (partial send-message target))
      (recur))))


(defn handle-outgoing [irc-connection channel]
  (go-loop []
    (let [event (<! channel)
          channel (:channel event)
          message (:message event)]
      (when-not (nil? event)
        (do
          (if (seq? message)
            (doseq [m message]
              (send-irc-message irc-connection channel m))
            (send-irc-message irc-connection channel message))
          (recur))))))


(defn setup-irc
  "Connect to the IRC network and join the configured channel."
  [shorten-url config]
  (let [incoming-messages (chan 10)
        outgoing-messages (chan 5)
        options (:irc config)
        irc-connection (irclj/connect
                        (:hostname options)
                        (:port options)
                        (:nickname options)
                        :callbacks {:raw-log events/stdout-callback
                                    :privmsg #(>!! incoming-messages %2)})]
    (handle-incoming incoming-messages shorten-url (partial say! outgoing-messages))
    (handle-outgoing irc-connection outgoing-messages)
    (irclj/join irc-connection (:channel options))
    (partial say! outgoing-messages)))
