(ns clj-dynamo.shortener
  (require [cheshire.core :as json]
           [org.httpkit.client :as http]))


(def ^:private api-uri "https://api-ssl.bitly.com/v3/shorten?access_token=%s&domain=bit.ly&longUrl=%s")


(defn handle-response
  "Returns the URL from the bitly response."
  [{body :body error :error}]
  (if error
    (println error)
    (get-in (json/parse-string body true) [:data :url])))


(defn shorten-url
  "Uses Bit.ly to shorten a URL."
  [access-token url]
  (let [response (http/get (format api-uri access-token url))
        short-url (handle-response @response)]
    (if short-url
      short-url
      url)))
