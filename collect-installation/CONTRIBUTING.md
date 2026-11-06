# Installing local dependencies

## Java runtime
The installer and the updater bundle an Eclipse Temurin JRE (64-bit only: linux-x64, windows-x64, osx).
It is not a Maven dependency: `jre-download.xml` downloads it from Adoptium during the build,
verifies its SHA-256 checksum and unpacks it into the folders used by the InstallBuilder project files.

To change version, update the `installer.jre.*` properties in `pom.xml`, including the checksums of the
JRE archives, listed by `https://api.adoptium.net/v3/assets/release_name/eclipse/jdk-<version>?image_type=jre`
(write the "+" of the version as `%2B`, e.g. `jdk-21.0.13%2B9`).

## Updaters and update feeds
Each installation reads the update feed whose URL is in `autoupdate/update.ini`. Every release builds
two updaters per platform and publishes two feeds (`-Ppublishupdater`):
- `update_3.xml`, read by 4.0.x installations (Java 8): lists the updaters that bundle the JRE
  (`collect-updater-<version>-<platform>`); they replace the `java` folder;
- `update_4.xml`, read by installations that already have the bundled JRE: lists the updaters without it
  (`collect-updater-<version>-<platform>-nojre`, built with `--setvars bundle_jre=0`), which refuse to run
  if the installed Java is older than 17.

The installer and both updaters write the `update_4.xml` URL (`collect.update_4.url`) into `update.ini`,
so an installation moves to the smaller updaters once it has the new JRE. Installations reading
`update_4.xml` keep their JRE: to ship a new one to them, make `update_4.xml` list the updaters with
the JRE and have the installer and the updaters write the URL of a new feed (`update_5.xml`).

## Install Saiku dependency locally
mvn install:install-file -Dfile=<path-to-file> -DgroupId=meteorite.bi -DartifactId=saiku-webapp-full -Dversion=2.6_b -Dpackaging=war
