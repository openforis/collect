# Contributing to Open Foris Collect 

You have found a bug or you have an idea for a cool new feature?
Contributing code is a great way to give something back to the open source community.
Before you dig right into the code, there are a few guidelines that we need contributors to follow so that we can have a chance of keeping on top of things.

## Getting Started with the development of Collect

In order to build and run Collect, you will have to install first these software:
* Java Development Kit (JDK) 8+
* Apache Maven 3.6+
* Node JS 12+

The project has been developed as a Maven project and it's divided into sub-modules.

### Default built modules

* collect-base (basic environment classes used even by Collect Mobile)
* collect-core (core functionalities, used even by Collect Earth and Collect Mobile)
* collect-rdb (Relational DataBase generator module).
* collect-server (server side module used by the web application).
* collect-webapp (client side module, written in React JS and ZK).

### Available Maven profiles

* installer: 		builds the all the modules, including collect-installation and all its sub-modules (collect-assembly, collect-autoupdater, collect-control-panel, collect-installer, collect-updater).
* publishupdater:	publish the InstallBuilder autoupdater files in the specified collect.autoupdater.ftp.host/remotefolder


### Maven POM properties
These properties can be specified in the Maven settings.xml file.

#### Mandatory properties

Property Name | Description
------------- | -------------
openforis.nexus.url | URL of the Nexus repository where latest artifacts have been released (e.g. https://oss.sonatype.org/content/repositories/releases if Sonatype Central repository is used); it's used to retrieve the latest version available and to check if an update is needed, and to generate the links to the updaters.)
collect.update.url | URL of update.xml file (used by InstallBuilder AutoUpdate to retrieve the new updaters)

#### VMWare InstallBuilder variables
They are used by collect-installer, collect-updater and collect-autoupdater modules, running with "installer" profile enabled.

Property Name | Description
------------- | -------------
installbuilder.builder.executable | InstallBuilder Builder executable file name (e.g. ${installbuilder.home}/bin/builder in Linux, ${installbuilder.home}/bin/builder.exe in Windows)
installbuilder.builder.output | InstallBuilder Builder output folder (the default one is ${installbuilder.home}/output)
installbuilder.autoupdate.executable | InstallBuilder AutoUpdate executable file name (e.g. customize.run for Linux, customize.exe for Windows) (used by collect-autoupdater module)
installbuilder.autoupdate.output | InstallBuilder AutoUpdate output folder (the default one is ${installbuilder.home}/autoupdate/output)

#### FTP update.xml uploader variables
They are used by collect-updater module, running with "publishupdater" profile enabled.
These variables are used to upload the update.xml file via FTP to the specified location (used by InstallBuilder AutoUpdater to fetch the new updaters).

Property Name | Description
------------- | -------------
collect.autoupdater.ftp.host | FTP server host name (used by collect-updater module to upload the update.xml file)
collect.autoupdater.ftp.username | FTP server username
collect.autoupdater.ftp.password | FTP server password
collect.autoupdater.ftp.remotefolder | FTP server remote folder (where to store the update.xml file)

### How to release a new version:

The release of a new version can be done using the Maven Release Plugin.
Please use the "installer" profile, otherwise the installers won't be generated.
In the perform goal, all the profiles will be used automatically, including "installer" of course.
Just run the following commands: 

```
mvn release:prepare -Pinstaller
mvn release:perform
```

## Setup Collect in Eclipse IDE

Configuring Collect in Eclipse can be a bit tricky, because some Maven plugins used by Collect are not working in this IDE.

If you want to configure Collect in Tomcat under Eclipse and publish automatically the changes to the resources, these are the main steps to follow:

- import all the projects as `Existing Maven Projects`. Install the plugins required during this process. Once done, if everything went well you shouldn't have any errors in every module.
- add Tomcat (version 9 or greater) to the Server Runtime Environments (if not done already)
- add some required libraries to the Tomcat lib folder: commons-pool-1.6.jar, commons-dbcp-1.4.jar, h2-1.4.193.jar, sqlite-jdbc-3.7.2.jar, postgresql-9.1-901.jdbc4.jar
- add the DB connection parameters as a Resource among the GlobalNamingResources in the Tomcat server.xml file. E.g. 

```
<Resource auth="Container" driverClassName="org.sqlite.JDBC" factory="org.apache.commons.dbcp.BasicDataSourceFactory" name="jdbc/collectDs" type="javax.sql.DataSource" url="jdbc:sqlite:${user.home}/OpenForis/CollectDev/data/collect.db"/>
```
(note that if you are using SQLite DB, the path to the .db file must exist).
- add the devMode=true parameter to the context.xml file:

```
   	<?xml version="1.0" encoding="UTF-8"?>
   	<Context>
   		...
   		<Parameter name="devMode" value="true" />
   	</Context>
```
- add the project collect-webapp to the Tomcat server
- startup Tomcat and check that you don't have any errors starting up Collect.
- start the NPM development server from command line: 

```
cd collect-webapp/frontend
npm start
```
Once started, Collect user interface will be available from the web browser at the address `http://localhost:3000` (please CHANGE IT INTO `http://127.0.0.1:3000` to avoid problems if running Collect with Google Chrome).
When running in dev mode, the web application will try to access the Collect RESTful API at the address `http://127.0.0.1:8080/collect/`; the CORS filter will prevent calls to the API from a different location, this is why you will have to use a plugin like Access Control Allow Origin (`https://mybrowseraddon.com/access-control-allow-origin.html`) to enable the connection to the server side.
