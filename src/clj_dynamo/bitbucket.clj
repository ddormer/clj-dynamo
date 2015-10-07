(ns clj-dynamo.bitbucket
  (:use clojure.java.io)
  (:require [ring.util.request :refer [body-string]]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [clj-dynamo.shortener :refer [shorten-url]]
            [clj-dynamo.irc-format :as fmt]))


(def pullrequest-uri
  "https://bitbucket.org/api/2.0/repositories/%s/pullrequests?state=OPEN")


(defn handle-response
  [{body :body} shorten-url]
  (let [prs (get-in (json/parse-string body true) [:values])]
    (map #(str (get-in % [:title]) " - " (shorten-url (get-in % [:links :html :href]))) prs)))


(defn open-pullrequests
  [shorten-url repo options]
  (let [response (http/get
                  (format pullrequest-uri repo)
                  {:basic-auth [(:username options) (:password options)]})]
    (handle-response @response shorten-url)))


(defn pullrequest-irc-message
  [shorten-url data action]
  (let [repo (get-in data [:repository :name])
        title (get-in data [:pullrequest :title])
        actor (get-in data [:actor :display_name])
        link (shorten-url (get-in data [:pullrequest :links :html :href]))]
    (format
     (str (fmt/color-text "[%s] " :cyan)
     "PR %s: %s | actor: %s | "
     (fmt/color-text "%s " :lightGrey)) repo action title actor link)))


(defn issue-irc-message
  [shorten-url data action]
  (let [repo (get-in data [:repository :name])
        title (get-in data [:issue :title])
        link (shorten-url (get-in data [:issue :links :html :href]))]
    (format
     (str (fmt/color-text "[%s] " :cyan)
          "%s "
          (fmt/color-text "%s " :purple)
          "| %s") repo action title link)))


(defmulti bitbucket-event (fn [event shorten-url data] event))

(defmethod bitbucket-event "issue:created"
  [_ shorten-url data]
  (issue-irc-message shorten-url data "New issue:"))


(defmethod bitbucket-event "pullrequest:created"
    [_ shorten-url data]
    (pullrequest-irc-message shorten-url data (fmt/color-text "Created" :lightGreen)))


(defmethod bitbucket-event "pullrequest:fulfilled"
  [_ shorten-url data]
  (pullrequest-irc-message shorten-url data (fmt/color-text "Merged" :blue)))


(defmethod bitbucket-event "pullrequest:approved"
  [_ shorten-url data]
  (pullrequest-irc-message shorten-url data (fmt/color-text "Approved" :green)))


(defmethod bitbucket-event "pullrequest:unapproved"
  [_ shorten-url data]
  (pullrequest-irc-message shorten-url data (fmt/color-text "Unapproved" :brown)))


(defmethod bitbucket-event "pullrequest:declined"
  [_ shorten-url data]
  (pullrequest-irc-message shorten-url data (fmt/color-text "Declined" :lightRed)))


(defmethod bitbucket-event :default
  [event _ _]
  (format "Recieved unimplemented Bitbucket event: %s" event))


(defn log-request
  [event body]
  (with-open [wrtr (writer
                    (format "/home/potato/code/Clojure/clj-dynamo/replay/%s.txt" event)
                    :append true)]
    (.write wrtr (format "%s\n" event))
    (.write wrtr body)))


(defn handle-bitbucket
  "Handle a Bitbucket event"
  [irc-message shorten-url request]
  (let [body (body-string request)
        event (get-in request [:headers "x-event-key"])]
    ;(log-request event body)
    (irc-message (bitbucket-event event shorten-url (json/parse-string body true)))
    ""))
