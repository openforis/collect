@echo off
SETLOCAL
FOR /F "tokens=1,2 delims==" %%A IN (tomcat\collect.properties) DO (SET %%A=%%B)
:: open default browser window using http_port found
start http://localhost:%http_port%/collect
ENDLOCAL
	
echo Starting up Tomcat...

cd tomcat/bin
startup.bat

cd ../..
