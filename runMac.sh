PROTEGE_PATH=/Applications/Protégé.app
PROTEGE_PLUGINS_PATH=$PROTEGE_PATH/Contents/Java/plugins
mvn clean install
rm $PROTEGE_PLUGINS_PATH/CoModIDE-1.1.1.jar
cp ./target/CoModIDE-1.1.1.jar $PROTEGE_PLUGINS_PATH/
open $PROTEGE_PATH
