# Installing local dependencies

The installer of Collect needs some pre-packed files containing an embedded version of Java JRE.
The ones currently being used are derived from OpenJDK and can be downloaded from https://www.openlogic.com/openjdk-downloads
Each JRE file for every type of OS (CLASSIFIER) must be:
- downloaded from https://www.openlogic.com/openjdk-downloads
- unzipped
- the root folder of the files should be renamed into jreVERSION-CLASSIFIER
- inside that folder there should be a folder called java-CLASSIFIER
- move all the content into that folder (bin, lib etc.)
- copy the file lib/java.xml into that folder
- zip the entire content including the root folder

Possible CLASSIFIER values: linux, linux-x64, osx, windows-i386, windows-x64
Current version: 1.8.0_272

## Install JRE locally
Once generated that files, they need to be installed in the local Maven repository.
Put the correct values into "path-to-file", "VERSION" and "CLASSIFIER" and run the following command:

mvn install:install-file -Dfile=<path-to-file> -DgroupId=net.java.openjdk -DartifactId=jre -Dversion=VERSION -Dpackaging=zip -Dclassifier=CLASSIFIER


## Install Saiku dependency locally
mvn install:install-file -Dfile=<path-to-file> -DgroupId=meteorite.bi -DartifactId=saiku-webapp-full -Dversion=2.6_b -Dpackaging=war
