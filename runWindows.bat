set PROTEGE_PATH="C:\Users\Owner\Desktop\IS\Protege-5.6.3-win\Protege-5.6.3\run.bat"
set PROTEGE_PLUGINS_PATH="C:\Users\Owner\Desktop\IS\Protege-5.6.3-win\Protege-5.6.3\plugins\"
del %PROTEGE_PLUGINS_PATH%CoModIDE-1.1.1.jar
copy target\CoModIDE-1.1.1.jar %PROTEGE_PLUGINS_PATH%
%PROTEGE_PATH%
