
FROM openjdk:8u121-alpine

MAINTAINER Yogev Mets <yogev.metzuyanim@logz.io>

RUN apk add --no-cache --update bash curl vim

ADD target/logzio-msgraph-0.0.3-app.jar /logzio-msgraph.jar

CMD java -jar logzio-msgraph.jar config.yaml