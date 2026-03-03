/* 
	IMAP configuration
*/

/*
	Configure server name, bind address, etc. here.
*/
setup(Packages.mireka.imap.server.ImapServer, {
	loginSpecification: setup(UserListLoginSpecification, {
		users: users
	}),
	repository: imapRepository,

	// Uncomment to specify TLS configuration specific to this service.
	/*
	tlsConfiguration: setup(Packages.mireka.imap.server.PrivateTlsConfiguration, {
		keystoreFile: "conf/localhost.p12",
	}),
	*/

	/* 
		Uncomment to use the global TLS configuration.
		Make sure that the global TLS configuration is valid, 
		otherwise an error will occur.
	*/
	tlsConfiguration: setup(Packages.mireka.imap.server.JsseDefaultTlsConfiguration),
});
