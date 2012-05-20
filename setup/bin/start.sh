#! /bin/bash
MIREKA_BIN_DIR=$(dirname $(readlink -f $0))
MIREKA_HOME=$(readlink -f  $MIREKA_BIN_DIR/..)
cd $MIREKA_HOME

SYSVARS="-Dlogback.configurationFile=conf/logback.xml"
SYSVARS="$SYSVARS -Dmireka.home=$MIREKA_HOME"
# authbind does not support IPv6
SYSVARS="$SYSVARS -Djava.net.preferIPv4Stack=true"

# Uncomment to wait for a debugger to connect before start
#DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,address=localhost:8000,suspend=y

if [ -z "$JAVA_CMD" ]; then
        JAVA_CMD=java
fi

authbind $JAVA_CMD -classpath classes:lib/*:conf $SYSVARS $DEBUG_OPTIONS mireka.startup.Start

