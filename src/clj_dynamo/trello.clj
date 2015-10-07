(ns clj-dynamo.trello
  (:require [cheshire.core :as json]
            [ring.util.request :refer [body-string]])
  (:import [org.apache.commons.codec.digest HmacUtils]
           [org.apache.commons.codec.binary Base64]))

(defn action-type
  [trello-map]
  (get-in trello-map [:action :type]))


(defmulti trello-irc-message (fn [shorten-url data] (keyword (action-type data))))

(defmethod trello-irc-message :createCard
  [shorten-url data]
  (let [actor (get-in data [:action :memberCreator :fullName])
        title (get-in data [:action :data :card :name])
        url (shorten-url (get-in data [:model :url]))]
    (println data)
    (format "%s has created a Trello card: %s %s" actor title url)))

(defmethod trello-irc-message :updateCard
  [shorten-url data]
  nil)


(defn verify-trello-request
  "Verify the request against the hash."
  [body hash secret callback-url]
  (= hash
     (Base64/encodeBase64String
      (HmacUtils/hmacSha1 secret (str body callback-url)))))


(defn trello-handler
  [irc-message, shortener, key, url, request]
  (let [body (body-string request)
        hash (get-in request [:headers "x-trello-webhook"])]
    (when (verify-trello-request body hash key url)
      (irc-message (trello-irc-message shortener (json/parse-string body true))))))
