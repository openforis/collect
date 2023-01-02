FROM maven:3.6.3-openjdk-11-slim as build

LABEL COMPANY="Open Foris"
LABEL MAINTAINER="openforisinitiative@gmail.com"
LABEL APPLICATION="Open Foris Collect"

WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y git
RUN mvn package

FROM tomcat:9.0.65-jdk11-openjdk-slim

COPY --from=build /app/collect-webapp/target/collect.war /usr/local/tomcat/webapps/

COPY collect-installation/docker/tomcat/conf/Catalina/localhost/collect.xml $CATALINA_HOME/conf/Catalina/localhost/collect.xml

COPY collect-installation/docker/tomcat/bin/setenv.sh $CATALINA_HOME/bin/setenv.sh
RUN chmod +x $CATALINA_HOME/bin/setenv.sh

ADD https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.40.0.0/sqlite-jdbc-3.40.0.0.jar $CATALINA_HOME/lib/
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.5.1/postgresql-42.5.1.jar $CATALINA_HOME/lib/
ADD https://repo1.maven.org/maven2/com/h2database/h2/2.1.214/h2-2.1.214.jar $CATALINA_HOME/lib/

EXPOSE 8080