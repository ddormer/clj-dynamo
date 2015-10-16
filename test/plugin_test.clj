(ns plugin-test
  (:import (java.lang IllegalStateException))
  (:require  [clojure.test :as t]
             [clj-dynamo.plugin :as plugin]))

(t/deftest validate-plugin-tests
  (t/testing "IllegalStateException is thrown if a plugin not implementing the Plugin protocol is added."
    (let [registry plugin/registry]
      (t/is (thrown? IllegalStateException
                     (swap! registry assoc (keyword (:command "testing")) "String, not implementation of the protocol")))))

  (t/testing "Plugins implementing the Plugin protocol are successfull added to the registry."
    (let [registry plugin/registry
          echo (plugin/->Echo "echo")]
      (plugin/register-plugin echo)
      (t/is (= echo (:echo @registry))))))
