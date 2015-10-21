(ns github-test
  (:require  [clojure.test :as t]
             [clj-dynamo.github :refer [verify-github-request]]))


(t/deftest verify-github-request-test
  (t/testing "Returns true if the hashes match."
    (t/is (true? (verify-github-request
                  "test data"
                  "sha1=0e632f04b286e5cfc07a7d27b17d25ac3fa024b0"
                  "12345"))))
  (t/testing "Will return false if the hashes don't match."
    (t/is (false? (verify-github-request "test data" "NOTAREALHASH" "12345")))))
