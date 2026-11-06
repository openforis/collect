# Installing local dependencies

## Java runtime
The installer and the updater bundle an Eclipse Temurin JRE (64-bit only: linux-x64, windows-x64, osx).
It is not a Maven dependency: `jre-download.xml` downloads it from Adoptium during the build,
verifies its SHA-256 checksum and unpacks it into the folders used by the InstallBuilder project files.

To change version, update the `installer.jre.*` properties in `pom.xml`, including the checksums of the
JRE archives, listed by `https://api.adoptium.net/v3/assets/release_name/eclipse/jdk-<version>?image_type=jre`
(write the "+" of the version as `%2B`, e.g. `jdk-21.0.13%2B9`).

## Install Saiku dependency locally
mvn install:install-file -Dfile=<path-to-file> -DgroupId=meteorite.bi -DartifactId=saiku-webapp-full -Dversion=2.6_b -Dpackaging=war
