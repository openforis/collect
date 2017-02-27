@echo off
::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: start.cmd (v2.0)                                   ::
:: Microsoft Windows OpenForis Collect startup script ::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: Set JRE_HOME
if "%JRE_HOME%"=="" (
	echo Setting JRE_HOME
    if exist "%ProgramFiles%\Java" (
		for /d %%i in ("%ProgramFiles%\Java\jre*") do set JRE_HOME=%%i
	) else if exist "%ProgramFiles% (x86)\Java" (
		for /d %%i in ("%ProgramFiles% (x86)\Java\jre*") do set JRE_HOME=%%i
	)
) else (
	echo Using already defined JRE_HOME
)

:: Check that variable is properly defined
if "%JRE_HOME%"=="" (
	echo Error: cannot determine JRE_HOME path automatically
	echo Please make sure you have Java Runtime Environment installed
	echo and define JRE_HOME environment variable in:
	echo     System Properties / Enviromnent Variables / System variables
	pause
) else (
	set JRE_HOME
	start http://localhost:8080/collect
	
	echo Starting up Tomcat...
	cd tomcat\bin
	call startup.bat
	cd ..\..
)
