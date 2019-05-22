PROTEGE_PATH=/Applications/Protégé.app
PROTEGE_PLUGINS_PATH=$PROTEGE_PATH/Contents/Java/plugins/
mvn clean install
rm $PROTEGE_PLUGINS_PATH/CoModIDE-1.0.0-SNAPSHOT.jar
cp ./target/CoModIDE-1.0.0-SNAPSHOT.jar $PROTEGE_PLUGINS_PATH/
open $PROTEGE_PATH
