echo off
setlocal
FOR /F "tokens=1,* delims=.=" %%G IN (tomcat/collect.properties) DO ( set %%G=%%H )

rem now use below vars
if "%%G"=="http_port"
 set COLLECT_TOMCAT_HTTP_PORT=%%H
echo %path%

endlocal
