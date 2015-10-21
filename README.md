# clj-dynamo
[![Build Status](https://travis-ci.org/ddormer/clj-dynamo.svg?branch=master)](https://travis-ci.org/ddormer/clj-dynamo)

IRC bot that notifies an IRC channel of changes to both Bitbucket and Github repositories.

Supported webhook events:
- Issues.
- Pull requests.

## Config
This bot was written for a specific use case so you'll have to fill in all the config fields for the moment, even if they're not used. *(i.e Trello)*

## Running
1. Edit config.json.
2. `lein uberjar`
3. `java -jar target/uberjar/clj-dynamo-0.1.0-standalone.jar --config /the/full/path/to/clj-dynamo/config.json`
