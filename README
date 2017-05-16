Default built modules:
- collect-core
- collect-rdb
- collect-server
- collect-web

Available Maven profiles:

- installer: 		builds the all the modules 
					(including collect-assembly, collect-control-panel, collect-autoupdater, 
					collect-installer, collect-updater, collect-updater-2, collect-saiku-updater)
- publishupdater:	publish the autoupdater in the specified collect.autoupdater.ftp.host/remotefolder
 

Required Maven properties:

- installbuilder.home						BitRock InstallBuilder home path (used by collect-installer and collect-udpater modules)
- installbuilder.builder.executable			Builder executable file name 
- installbuilder.autoupdate.executable		Autoupdate executable file name (e.g. customize.run for Linux, customize.exe for Windows) (used by collect-autoupdater module)
- installbuilder.autoupdate.output			Autoupdate output folder (default is ${installbuilder.home}/autoupdate/output)

- collect.autoupdater.ftp.host				FTP server host name (used by collect-updater module to upload the update.xml file)
- collect.autoupdater.ftp.username 			FTP server username
- collect.autoupdater.ftp.password			FTP server password
- collect.autoupdater.ftp.remotefolder		FTP server remote folder (where to store the update.xml file)
- collect.wiki.idm.expression-language.url	URL to be opened to have information on the IDM expression language

How to release a new version:

mvn release:prepare
mvn release:perform

(All profiles are active during the release).

See http://www.openforis.org/wiki/index.php/Open_Foris_Collect_Development_Environment for detailed instructions/
