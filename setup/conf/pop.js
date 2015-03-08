/* 
	POP3 and POP3 Mail Importer configuration.
*/

/*
	Uncomment to import POP3 mails from an existing server at 
	startup for each user. The user names and the passwords must 
	match on both systems.
*/
/*
setup(PopMailImporter, {
	users: globalUsers,
	maildropRepository: maildropRepository,
	remoteHost: "localhost",
	remotePort: 110,
});
*/

/*
	Configure server name, bind address, etc. here.
*/
setup(PopServer, {
	loginSpecification: setup(GlobalUsersLoginSpecification, {
		users: globalUsers
	}),
	principalMaildropTable: setup(GlobalUsersPrincipalMaildropTable),
	maildropRepository: maildropRepository,

	// Uncomment to specify TLS configuration specific to this service.
	/*
	tlsConfiguration: setup(PrivateTlsConfiguration, {
		enabled: true
	}),
	*/

	/* 
		Uncomment to use the global TLS configuration.
		Make sure that the global TLS configuration is valid, 
		otherwise an error will occur.
	*/
	/*
	tlsConfiguration: setup(JsseDefaultTlsConfiguration),
	*/
});
