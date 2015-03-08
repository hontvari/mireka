@ECHO OFF
pushd "%~dp0"\..
set SYSVARS=-Dlogback.configurationFile=conf/logback.xml
set SYSVARS=%SYSVARS% -Dmireka.home="%cd%"

rem Uncomment to wait for a debugger to connect before start
rem set DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,address=localhost:8000,suspend=y

rem "C:\Program Files\Java\jre6\bin"
java -classpath classes;lib\*;conf %SYSVARS% %DEBUG_OPTIONS% mireka.startup.Start
popd
