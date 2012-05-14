/* 
	Public SMTP service configuration. This is the service which usually 
	runs on port 25.
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/

/*
	Accepted recipients for the proxy mode. Mails to these 
	recipients will be relayed to a backend server. 
	
	Note: In order to make most of this file identical for both
	proxy and standalone server mode, this list is allowed to have 
	a common subset with the localRecipientsTable 
	(defined in mireka.xml). This does not cause any problem,
	because that table is queried first, so it has a 
	higher priority (see below). If a recipient here also 
	appears on the localRecipientsTable, then a mail addressed to 
	the recipient will not be relayed, but it will be delivered 
	according to its destination assigned in localRecipientsTable.
*/
relayedRecipients = setup(RecipientSpecifications, {
	specifications: list(
		setup(GlobalPostmasterSpecification),
		/* this matches Postmaster@... addresses for any domain, not 
			only for local domains, but non-local ones will be 
			rejected by the ProhibitRelaying filter anyway */
		setup(AnyDomainPostmaster),
		setup(InlineRecipientRegistry, {
			addresses: include("conf/mx/proxy-individual-recipients.js")
		}),
		setup(GlobalUsersRecipientSpecification, {
				users: globalUsers
		}),
		include("conf/mx/proxy-wildcard-recipients.js")
	)
});
	
/*
	This table specifies valid recipients and assigns destinations 
	to them. 
*/
recipientTable = setup(RecipientTable, {
	mappers: [
		
		/* 
			Any recipient included in the localRecipientsTable
			will be delivered according to that table.
		*/
		localRecipientsTable,
		
		/*
			Any other recipient, which is still valid according to
			the relayedRecipients table above, 
			is relayed to the backend server.
		*/
		setup(RecipientSpecificationDestinationPair, {
			recipientSpecification: relayedRecipients,
			destination: setup(RelayDestination, {
				backendServer: backendServer
			})
		})
	]
});

/*
	The filter chain.
	
	It is unlikely that you need to change this, except if you use
	a custom filter.
*/
mxFilters = setup(Filters, {
	filters: [
		setup(MeasureTraffic, {
			trafficSummary: setup(TrafficSummary, {
				objectName: "mireka:type=TrafficSummary,name=MX"
			}))
		}),
		setup(LookupDestinationFilter, {
				recipientDestinationMapper: recipientTable
		}),
		setup(RejectLargeMail),
		setup(AddReceivedSpfHeader),
		setup(TarpitOnGlobalRejections),
		setup(AcceptGlobalPostmaster),
		setup(ProhibitRelaying, {
				localDomainSpecifications: [ localDomains ]
		}),
		setup(AcceptDomainPostmaster),
		setup(RefuseBlacklistedRecipient, {
			blacklists: [
				setup(Dnsbl, {
					domain: "zen.spamhaus.org.",
				})
			]
		}),
		setup(RefuseUnknownRecipient),
		setup(RejectOnFailedSpfCheck),
		setup(AcceptAllRecipient),
		setup(SavePostmasterMail, {
			dir: "postmaster",
		}),
		setup(StopLoop),
		setup(DestinationProcessorFilter)
	]
});

/* Do not change this */
mxMessageHandler = setup(MessageHandlerFactoryImpl, {
	filters: mxFilters,
});

/*
	Configure server name, bind address, etc. here.
*/
mx = setup(SMTPServer(mxMessageHandler), {
	hostName: helo,
	/* bindAddress: "192.0.2.0", */
	/* uncomment to enable STARTTLS if JSSE is correctly configured */
	/* enableTLS: true, */
}));
	