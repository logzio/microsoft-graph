
FROM openjdk:8-jre-alpine

MAINTAINER Tamir Michaeli <tamir.michaeli@logz.io>

RUN apk --no-cache add curl

RUN wget https://github.com/logzio/microsoft-graph/releases/download/v0.0.5/logzio-msgraph-0.0.5-app.jar

RUN cp logzio-msgraph-0.0.5-app.jar /logzio-msgraph.jar

CMD java -jar logzio-msgraph.jar config.yaml
