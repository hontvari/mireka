#!/bin/sh
### BEGIN INIT INFO
# Provides:          jddclient
# Required-Start:    $local_fs $remote_fs $syslog $network
# Required-Stop:     $local_fs $remote_fs $syslog $network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Mireka mail server and SMTP proxy
# Description:       Mireka init script
### END INIT INFO

# This file is based on the similar tomcat6 init.d script and
# /etc/init.d/skeleton

# Do NOT "set -e"

# PATH should only include /usr/* if it runs after the mountnfs.sh script
PATH=/sbin:/usr/sbin:/bin:/usr/bin
DESC="Mireka mail server and SMTP proxy"
NAME=mireka
DAEMON=/usr/bin/jsvc
PIDFILE=/var/run/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME
DEFAULT=/etc/default/$NAME
APPLICATION_HOME=/opt/$NAME
LIB=$APPLICATION_HOME/lib
USER=$NAME
JAVA_HOME=/opt/jre1.7.0_04

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.0-6) to ensure that this file is present.
. /lib/lsb/init-functions

# setup JAVA_HOME
#. /usr/lib/java-wrappers/java-wrappers.sh
#find_java_runtime java7

# construct a classpath from lib jars
LIBCLASSPATH=
for i in `ls $LIB/*.jar`
do
  LIBCLASSPATH=${LIBCLASSPATH}:${i}
done

JSVC_CLASSPATH="/usr/share/java/commons-daemon.jar:classes:$LIBCLASSPATH:conf"
JAVA_OPTS="-Dlogback.configurationFile=conf/logback.xml -Dmireka.home=$APPLICATION_HOME"
BOOTSTRAP_CLASS=mireka.startup.Daemon
DAEMON_OPTS=""

# overwrite settings from default file
if [ -f "$DEFAULT" ]; then
	. "$DEFAULT"
fi


#
# Function that starts the daemon/service
#
do_start()
{
	# Return
	#   0 if daemon has been started
	#   1 if daemon was already running
	#   2 if daemon could not be started
	start-stop-daemon --test --start --pidfile $PIDFILE \
                --startas "$JAVA_HOME/bin/java" \
		>/dev/null \
		|| return 1
	umask 027
	cd "$APPLICATION_HOME"	
	$DAEMON -home "$JAVA_HOME" -cp "$JSVC_CLASSPATH" -user $USER \
	    -outfile SYSLOG -errfile SYSLOG \
	    -pidfile "$PIDFILE" $JAVA_OPTS \
	    "$BOOTSTRAP_CLASS" $DAEMON_OPTS \
	    || return 2
}

#
# Function that stops the daemon/service
#
do_stop()
{
        # Return
        #   0 if daemon has been stopped
        #   1 if daemon was already stopped
        #   2 if daemon could not be stopped
        #   other if a failure occurred
	if start-stop-daemon --test --start --pidfile "$PIDFILE" \
		--startas "$JAVA_HOME/bin/java" >/dev/null \
		; then
			return 1
	else 
		$DAEMON -home "$JAVA_HOME" -cp "$JSVC_CLASSPATH" \
			-pidfile "$PIDFILE" \
			-stop "$BOOTSTRAP_CLASS" \
			|| return 2
		return 0
	fi
}

case "$1" in
  start)

	log_daemon_msg "Starting $DESC" "$NAME"
	do_start
	case "$?" in
		0) log_end_msg 0 ;;
		1) log_end_msg 0 ;;
		2) log_end_msg 1 ;;
	esac
	;;
  stop)
	log_daemon_msg "Stopping $DESC" "$NAME"
	do_stop
	case "$?" in
		0) log_end_msg 0 ;;
		1) log_end_msg 0 ;;
		2) log_end_msg 1 ;;
	esac
	;;
  status)
        if start-stop-daemon --test --start --pidfile "$PIDFILE" \
                --startas "$JAVA_HOME/bin/java" \
		>/dev/null \
		; then

		if [ -f "$PIDFILE" ]; then
		    log_success_msg "$DESC $NAME is not running, but pid file exists."
			exit 1
		else
		    log_success_msg "$DESC $NAME is not running."
			exit 3
		fi
	else
		log_success_msg "$DESC $NAME is running with pid `cat $PIDFILE`"
	fi
        ;;
  restart|force-reload)
	#
	# If the "reload" option is implemented then remove the
	# 'force-reload' alias
	#
	log_daemon_msg "Restarting $DESC" "$NAME"
	do_stop
	case "$?" in
	  0|1)
		do_start
		case "$?" in
			0) log_end_msg 0 ;;
			1) log_end_msg 1 ;; # Old process is still running
			*) log_end_msg 1 ;; # Failed to start
		esac
		;;
	  *)
	  	# Failed to stop
		log_end_msg 1
		;;
	esac
	;;
  *)
	echo "Usage: $SCRIPTNAME {start|stop|status|restart|force-reload}" >&2
	exit 3
	;;
esac

