# Installing local dependencies

## Install JRE locally

mvn install:install-file -Dfile=<path-to-file> -DgroupId=net.java.openjdk -DartifactId=jre -Dversion=1.8.0_272 -Dpackaging=zip -Dclassifier=linux
mvn install:install-file -Dfile=<path-to-file> -DgroupId=net.java.openjdk -DartifactId=jre -Dversion=1.8.0_272 -Dpackaging=zip -Dclassifier=linux-x64
mvn install:install-file -Dfile=<path-to-file> -DgroupId=net.java.openjdk -DartifactId=jre -Dversion=1.8.0_272 -Dpackaging=zip -Dclassifier=osx
mvn install:install-file -Dfile=<path-to-file> -DgroupId=net.java.openjdk -DartifactId=jre -Dversion=1.8.0_272 -Dpackaging=zip -Dclassifier=windows-i386
mvn install:install-file -Dfile=<path-to-file> -DgroupId=net.java.openjdk -DartifactId=jre -Dversion=1.8.0_272 -Dpackaging=zip -Dclassifier=windows-x64


## Install Saiku dependency locally
mvn install:install-file -Dfile=<path-to-file> -DgroupId=meteorite.bi -DartifactId=saiku-webapp-full -Dversion=2.6_b -Dpackaging=war
