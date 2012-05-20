#! /bin/bash
MIREKA_HOME=$(dirname $(readlink -f $0))/..
cd $MIREKA_HOME

SYSVARS=-Dlogback.configurationFile=conf/logback.xml
SYSVARS="$SYSVARS -Dmireka.home=$MIREKA_HOME"

# Uncomment to wait for a debugger to connect before start
#DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,address=localhost:8000,suspend=y

if [ -z "$JAVA_CMD" ]; then
        JAVA_CMD=java
fi

$JAVA_CMD -classpath classes:lib/*:conf $SYSVARS $DEBUG_OPTIONS mireka.startup.Start
