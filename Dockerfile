FROM maven:3.6.3-openjdk-11-slim as build
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y git
RUN mvn package

FROM tomcat:9.0.65-jdk11-openjdk-slim
MAINTAINER Open Foris
COPY --from=build /app/collect-webapp/target/collect.war /usr/local/tomcat/webapps/
COPY collect-installation/collect-assembly/src/main/assembly/resources/tomcat/bin/setenv.sh $CATALINA_HOME/bin/setenv.sh
RUN chmod +x $CATALINA_HOME/bin/setenv.sh
