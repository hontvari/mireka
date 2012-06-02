/*
	List of local domains. These are the domains which are served by this 
	mail server. 
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/

localDomains = setup(InlineDomainRegistry, {
	remoteParts: [
		// "example.com",
		// "[192.0.2.0]",
	]
});