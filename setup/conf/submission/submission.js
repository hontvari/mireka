/* 
	SMTP Submission service configuration. This is the service which usually
	runs on port 587.
*/
	
/* 
	Comment out if mails will be relayed through the backend server.
	In that case queues are not necessary. 
*/
include("conf/submission/queues.js");

/*
	This table assigns a destination to any recipient addresses.
*/
submissionRecipientTable = setup(RecipientTable, {

	mappers: [
			
		/*
			Local recipients will be delivered according to their
			destination as specified in localRecipientsTable.
		*/
		localRecipientsTable,
		
		/*
			Any other recipients will be either transmitter directly 
			to the remote domain or relayed to the backend server.
		*/
		setup(RecipientSpecificationDestinationPair, {
			recipientSpecification: setup(AnyRecipient),
			
			/* 
				comment out if all mails will be 
				relayed through the backend server
			*/
			destination: setup(TransmitterDestination),
					
			/* 
				uncomment to relay all mail to 
				the backend server 
			*/
			/*
			destination: setup(RelayDestination, {
				backendServer: backendServer
			})
			*/
		})
				
	]
});

/*
	The filter chain.
	
	It is unlikely that you need to change this, except if you use
	a custom filter.
*/
submissionFilters = setup(Filters, {
	
	filters: [
		setup(MeasureTraffic, {
			incomingSmtpSummary: setup(IncomingSmtpSummary, {
				name: "submission"
			})
		}),
		setup(RejectIfUnauthenticated, {
			authenticatedSpecifications: [
				setup(SmtpAuthenticated),
				setup(ConnectedFromAuthorizedIpAddress, {
					addresses: ipAddressList(include("conf/submission/authorized-ip.js"))
				})
			]
		}),
		setup(LookupDestinationFilter, {
			recipientDestinationMapper: submissionRecipientTable
		}),
		setup(RejectLargeMail),
		setup(AcceptGlobalPostmaster),
		setup(AcceptDomainPostmaster),
		setup(AcceptAllRecipient),
		setup(SavePostmasterMail, {
			dir: "postmaster",
		}),
		setup(StopLoop),
		setup(DestinationProcessorFilter)
	]
});

/*
	Authentication
*/
usernamePasswordValidator = setup(UsernamePasswordValidatorImpl, {
	loginSpecification: setup(GlobalUsersLoginSpecification, {
		users: globalUsers
	})
});

/* Do not change this */
submissionMessageHandler = setup(MessageHandlerFactoryImpl, {
	filters: submissionFilters
});

authenticationHandlerFactory = 
	new Packages.org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory(usernamePasswordValidator);

/*
	Configure server name, bind address, etc. here.
*/
submission = setup(SubmissionServer(submissionMessageHandler), {
	authenticationHandlerFactory: authenticationHandlerFactory,
	// bindAddress: "192.0.2.0",
	hostName: helo,
	/* uncomment to enable STARTTLS if JSSE is correctly configured */
	/* enableTLS: true, */
});
	