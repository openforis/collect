FROM maven:3.9.16-eclipse-temurin-21 AS build

LABEL COMPANY="Open Foris"
LABEL MAINTAINER="openforisinitiative@gmail.com"
LABEL APPLICATION="Open Foris Collect"

WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y git
RUN mvn package

FROM tomcat:9.0.123-jdk21-temurin

COPY --from=build /app/collect-webapp/target/collect.war /usr/local/tomcat/webapps/

COPY collect-installation/docker/tomcat/conf/Catalina/localhost/collect.xml $CATALINA_HOME/conf/Catalina/localhost/collect.xml

COPY collect-installation/docker/tomcat/bin/setenv.sh $CATALINA_HOME/bin/setenv.sh
RUN chmod +x $CATALINA_HOME/bin/setenv.sh

ADD https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.54.1.0/sqlite-jdbc-3.54.1.0.jar $CATALINA_HOME/lib/
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar $CATALINA_HOME/lib/
ADD https://repo1.maven.org/maven2/com/h2database/h2/2.5.252/h2-2.5.252.jar $CATALINA_HOME/lib/

EXPOSE 8080