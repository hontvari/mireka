#!/bin/sh
### BEGIN INIT INFO
# Provides:          mireka
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
PIDFILE=/var/run/$NAME.pid
DEFAULT=/etc/default/$NAME
APP_HOME=/opt/$NAME
USER=$NAME

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.0-6) to ensure that this file is present.
. /lib/lsb/init-functions

# setup JAVA_HOME
. /usr/lib/java-wrappers/java-wrappers.sh
# The java-wrappers in Ubuntu 10.04 goes into an endless loop if java7
# is specified, which is unknown for it
if [ -n "$__jvm_java7" ]; then
        find_java_runtime java7
fi

CLASSPATH="classes:lib/*:conf"
JAVA_OPTS="-Dlogback.configurationFile=conf/logback.xml"
JAVA_OPTS="$JAVA_OPTS -Dmireka.home=$APP_HOME"
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"

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
	
	# uncomment --background and add --verbose to debug
	start-stop-daemon --start --pidfile $PIDFILE \
		--chuid $USER --chdir $APP_HOME \
		--background \
		--make-pidfile \
                --startas "/usr/bin/authbind" \
                -- $JAVA_HOME/bin/java -cp $CLASSPATH $JAVA_OPTS mireka.startup.Start
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
	start-stop-daemon --stop --pidfile $PIDFILE
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
		0) log_end_msg 0 ; rm $PIDFILE ;;
		1) log_end_msg 0 ; rm $PIDFILE ;;
		2) log_end_msg 1 ;;
	esac
	;;
  status)
        if start-stop-daemon --test --start --pidfile "$PIDFILE" \
                --startas "/usr/bin/authbind" \
		>/dev/null \
		; then

		if [ -f "$PIDFILE" ]; then
		    log_success_msg "$DESC is not running, but pid file exists."
			exit 1
		else
		    log_success_msg "$DESC is not running."
			exit 3
		fi
	else
		log_success_msg "$DESC is running with pid `cat $PIDFILE`"
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
	echo "Usage: $0 {start|stop|status|restart|force-reload}" >&2
	exit 3
	;;
esac

