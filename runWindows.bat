set PROTEGE_PATH="C:\Program Files\Protege-5.5.0\run.bat"
set PROTEGE_PLUGINS_PATH="C:\Program Files\Protege-5.5.0\plugins\"
del %PROTEGE_PLUGINS_PATH%CoModIDE-1.0.0-SNAPSHOT.jar
copy target\CoModIDE-1.0.0-SNAPSHOT.jar %PROTEGE_PLUGINS_PATH%
%PROTEGE_PATH%
