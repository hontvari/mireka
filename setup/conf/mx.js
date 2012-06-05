/* 
	Public SMTP service configuration. This is the service which usually 
	runs on port 25.
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/

/*
	The filter chain.
	
	It is unlikely that you need to change this, except if you use
	a custom filter.
*/
mxFilters = setup(Filters, {
	filters: [
		setup(MeasureTraffic, {
			incomingSmtpSummary: setup(IncomingSmtpSummary, {
				name: "mx"
			})
		}),
		setup(LookupDestinationFilter, {
				recipientDestinationMapper: localRecipientsTable
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
});
	