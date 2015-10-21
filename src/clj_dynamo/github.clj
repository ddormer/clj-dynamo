(ns clj-dynamo.github
  (:require [cheshire.core :as json]
            [ring.util.request :refer [body-string]]
            [clj-dynamo.irc-format :as fmt])
  (:import [org.apache.commons.codec.digest HmacUtils]
           [org.apache.commons.codec.binary Hex]))


(defmulti github-event (fn [event shorten-url data] event))

(defmethod github-event "issues"
  [_ shorten-url data]
  (let [repo (get-in data [:repository :name])
        title (get-in data [:issue :title])
        link (shorten-url (get-in data [:issue :html_url]))
        action (:action data)]
    (format
     (str (fmt/color-text "[%s] " :cyan)
          "Issue %s: "
          (fmt/color-text "%s " :purple)
          "| %s") repo action title link)))


(defmethod github-event "pull_request"
  [_ shorten-url data]
  (let [repo (get-in data [:repository :name])
        title (get-in data [:pull_request :title])
        link (shorten-url (get-in data [:pull_request :html_url]))
        actor (get-in data [:sender :login])
        action (:action data)]
    (format
     (str (fmt/color-text "[%s] " :cyan)
          "PR %s: "
          (fmt/color-text "%s" :purple)
          " | actor: %s | "
          (fmt/color-text "%s " :lightGrey)) repo action title actor link)))


(defmethod github-event :default
  [event _ _]
  (format "Recieved unimplemented Github event: %s" event))


(defn verify-github-request
  [request signature secret-key]
  (= signature
     (str "sha1=" (HmacUtils/hmacSha1Hex secret-key request))))


(defn handle-github
  "Handle a Github event"
  [irc-message shorten-url config request]
  (let [body (body-string request)
        event (get-in request [:headers "x-github-event"])
        signature (get-in request [:headers "x-hub-signature"])]
    (when (verify-github-request body signature (:secret-key config))
      (irc-message (github-event event shorten-url (json/parse-string body true))))
    ""))
