# Open Foris Collect

Easy and flexible survey design and data management

Open Foris Collect is the main entry point for data collected in field-based inventories. It provides a fast, easy, flexible way to set up a survey with a user-friendly interface.
Collect handles multiple data types and complex validation rules, all in a multi-language environment.

Developed under the [Open Foris Initiative](http://www.openforis.org)

## Key Features

* **User Friendliness**: Nice web interface; Designed based on real users’ needs, No need for technical skills to use it.
* **Rapid Data Entry**: Limited use of mouse needed; Data entry using only keyboard; Auto-complete; Species list search; Immediate feedback on errors/warnings.
* **Highly Configurable**: Design the survey from scratch or starting from a template; Data entry user interface is automatically generated and metadata driven; Validation rules (distance, comparison, pattern...); Multiple layouts (form, table, multiple columns form).
* **Multiple data types**: Basic Types – Text, Number, Boolean, Date, Time. Complex types – Range, Coordinate, File, Taxon. Plus, support for calculated values.
* **Multi-user or standalone**: It can be used in a standalone environment with no need for internet connection; Data can be exported from single/standalone installations and imported into a centralized installation to create a complete data set; In multi-user environment, users can work only on owned records.
* **Controlled QA workflow**: Record goes through different steps: Data entry, Data cleansing, Data analysis. Minimized "data cooking". 
* **Rich metadata**: XML format, Complex nested structure of the survey, Validation rules, Multiple Spatial Reference Systems.
* **Multilingual**: Define the survey in multiple languages - Tab labels, Input field labels, Validation messages, Code item labels, Element info tooltips. The user will see the survey in the language of his/her web browser or in the survey default language.
* **Multiple data export/import formats**: XML, CSV, Relational database. 

## Where to download the installer?

If you are not interested in the code but rather on the Collect features you might want to run it right away!
Go to our [website](http://www.openforis.org/tools/collect.html) and download the installer directly there. There are versions for Windows, Mac OS X and Linux 32-bit and 64-bit. 

## Do you have any questions?

Please register into our [Community Support Forum](http://www.openforis.org/support) and raise your question or feature request there. 

## Getting Started with the development of Collect

In order to build and run Collect, you will have to install first these software:
* Java Development Kit (JDK) 8+
* Apache Maven 3.6+
* Node JS 12+

The project has been developed as a Maven project and it's divided into sub-modules.

### Default built modules

* collect-base (basic environment classes used even by Collect Mobile)
* collect-client (client side module, written in Adobe Flex)
* collect-core (core functionalities, used even by Collect Earth and Collect Mobile)
* collect-rdb (Relational DataBase generator module).
* collect-server (server side module used by the web application).
* collect-webapp (client side module, written in React JS and ZK).

### Available Maven profiles

* installer: 		builds the all the modules, including collect-installation and all its sub-modules (collect-assembly, collect-autoupdater, collect-control-panel, collect-installer, collect-updater, collect-updater-2, collect-saiku-updater).
* publishupdater:	publish the InstallBuilder autoupdater files in the specified collect.autoupdater.ftp.host/remotefolder
 

### Required Maven properties

* installbuilder.home						BitRock InstallBuilder home path (used by collect-installer and collect-udpater modules)
* installbuilder.builder.executable			Builder executable file name 
* installbuilder.autoupdate.executable		Autoupdate executable file name (e.g. customize.run for Linux, customize.exe for Windows) (used by collect-autoupdater module)
* installbuilder.autoupdate.output			Autoupdate output folder (default is ${installbuilder.home}/autoupdate/output)

* collect.nexus.url 						URL of the Nexus repository where latest artifacts have been released (e.g. https://oss.sonatype.org/content/repositories/releases if Sonatype Central repository is used); it's used to retrieve the latest version available and to check if an update is needed, and to generate the links to the updaters.)
* collect.autoupdater.ftp.host				FTP server host name (used by collect-updater module to upload the update.xml file)
* collect.autoupdater.ftp.username 			FTP server username
* collect.autoupdater.ftp.password			FTP server password
* collect.autoupdater.ftp.remotefolder		FTP server remote folder (where to store the update.xml file)

### How to release a new version:

The release of a new version can be done using the Maven Release Plugin.
Just run the following commands: 

```
mvn release:prepare
mvn release:perform
```

(All profiles are active during the release).

## License

Collect and the rest of the Open Foris tools follow the MIT License, meaning that you can do anything you want with the code! Of course we appreciate references to our project, [Open Foris](www.openforis.org)