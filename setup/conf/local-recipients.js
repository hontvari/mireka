/*
	Recipient-destination mappings.
	
	The list enumerates all valid local recipients and their destinations.
	
	A destination tells what to do with a mail sent to the corresponding 
	recipient. E.g. drop the mail or store it in a maildrop or 
	forward it to other addresses.  
*/

localRecipientDestinations = deepList([

	/*
		ENTER YOUR RECIPIENT-DESTINATION MAPPINGS HERE
	*/
	
	
	
	/* 
		comment out if POP service is not active
	*/
	setup(GlobalUsersMaildropDestinationMapper, {
		maildropRepository: maildropRepository,
		users: globalUsers
	}),
	
	/*
		Postmaster alias
		
		Specify a - preferably local - mailbox address which will be the 
		alias of both the server-wide "Postmaster" and the domain 
		specific local "Postmaster@..." addresses.
		
		Comment it out if this server proxies postmaster mails to
		another server.
	*/
	setup(PostmasterAliasMapper, {
		canonical: "john@example.com"
	}),
	
	/*
		comment out if forwarding is not used
	*/
	setup(RecipientSpecificationDestinationPair, {
		recipientSpecification: setup(SrsRecipientSpecification),
		destination: setup(SrsDestination)
	}),
]);
