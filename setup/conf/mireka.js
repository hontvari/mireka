/*
	The main configuration file, which is read and executed by Mireka 
	during startup. All other configuration scripts are included directly
	or indirectly by this script. In addition to the standard EcmaScript 
	objects, the script can access the mireka.startup.ScriptApi object using the
	"configuration" variable.  
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/

configuration.include("lib/configuration.js");
include("conf/circular.js");

/*
	The default host name used to identify this server, for example
	in the SMTP HELO command. This value is referred at several 
	places in the configuration files.
*/
var helo="mail.example.com";

/* 
	Uncomment to set global JSSE TLS settings. This is not necessary 
	if TLS is not used or if it is configured for each service 
	separately.
*/
/*
System.setProperty("javax.net.ssl.keyStore", "conf/keystore.jks");
System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
*/
	
/*
	Default SMTP client factory, it specifies the properties of the 
	outgoing SMTP connections.
*/
clientFactory = setup(ClientFactory, {
	helo: helo,
	// bind: "192.0.2.0",
});

/*
	Default backend server. It specifies an SMTP server, to where 
	mails are relayed for delivery or submission. It can be 
	referred where SMTP proxy functionality is configured. 
*/
backendServer = setup(BackendServer, {
	host: "backend.example.com",
	clientFactory: clientFactory 
});

/*
	Default maildrop repository. A maildrop corresponds to a POP3
	account. Maildrops are used to store incoming mails (via SMTP) 
	and to transfer mails to the user (via POP3). 
*/
maildropRepository = setup(MaildropRepository, {
	dir: "maildrops"
});

include("conf/domains.js");

/* 
	comment out if none of the submission and POP3 services are 
	active
*/
include("conf/global-users.js");

/*
	Sender Rewriting Scheme (SRS) configuration. SRS is used to 
	make forwarding working even if receivers check SPF records.
*/
srs = setupDefault(Srs, {
	/* 
		Key for signing SRS encoded reverse paths. 
		Uncomment and enter a random hexadecimal string here, 
		like 37AB...
		You may want to use http://www.fourmilab.ch/hotbits/
		to get random bits.
	*/
	// secretKey: "ENTER A HEX KEY HERE",
	localDomains: localDomains
});

/*
	Default local recipients table. It enumerates all valid local 
	recipients and assigns destinations to them. The destination
	tells what to do with a mail sent to the corresponding 
	recipient. E.g. drop the mail or store it in a maildrop or 
	forward it to other addresses. 
*/
localRecipientsTable = setup(LocalRecipientTable, {
	localDomains: localDomains,
	mappers: include("conf/local-recipients.js")
});

/*
	comment out to disable the POP3 server (port 110)
*/
include("conf/pop.js");

/*
	comment out to disable the RFC 4409 message submission service (port 587)

	This service receives messages from the local users and - depending on 
	its configuration - either directly transmits messages to the mail 
	servers of the recipient on the internet, or relays messages to a 
	backend server.
*/
include("conf/submission/submission.js");

/*
	comment out to disable the MX (receiving) SMTP server (port 25)
	
	This service waits for messages from the public internet and - depending 
	on its configuration - either relay a message to a backend server, or 
	deliver it into a local POP3 maildrop. 
*/
include("conf/mx.js");

/*
	uncomment to send monitoring data to a Graphite server for graphical 
	viewing
*/
/*
setup(GraphiteReporter, {
	host: "graphite.example.com",
	prefix: "mail.mireka"
});
*/
	

