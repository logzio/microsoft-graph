
FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine

MAINTAINER Tamir Michaeli <tamir.michaeli@logz.io>

RUN apk --no-cache add curl

RUN wget https://github.com/logzio/microsoft-graph/releases/download/v0.0.7/logzio-msgraph-0.0.7-app.jar

RUN cp logzio-msgraph-0.0.7-app.jar /logzio-msgraph.jar

CMD java -jar logzio-msgraph.jar config.yaml
