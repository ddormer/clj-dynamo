FROM debian:jessie
MAINTAINER "Darren Dormer <me@darren.io>"
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -qy openjdk-7-jre-headless
COPY ["target/uberjar/clj-dynamo-0.1.0-standalone.jar", "/srv/dynamo/clj-dynamo.jar"]
COPY ["config.json", "/srv/dynamo/config.json"]
WORKDIR /srv/dynamo
ENTRYPOINT ["/usr/bin/java", "-jar", "/srv/dynamo/clj-dynamo.jar", "--config", "/srv/dynamo/config.json"]
EXPOSE 8080
