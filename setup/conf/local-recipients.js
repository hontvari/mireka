/*
	Recipient-destinatin mappings for the local recipients table, named
	localRecipientsTable and defined in mireka.js.
	
	The list enumerates all valid local recipients and their destinations.
	
	The containing object of this list, localRecipientsTable, filters 
	recipients by checking if they are local addresses based on their remote 
	part. If the result is that the recipient is not local, then the 
	following mappings are not checked at all. For example if this list 
	contains a wildcard mapping like "any recipient", then that practically 
	means "any LOCAL recipient".
	
	A destination tells what to do with a mail sent to the corresponding 
	recipient. E.g. drop the mail or store it in a maildrop or 
	forward it to other addresses.  
	
	
	Examples
	========
	
	The examples below are usually given in two forms. The short form is
	more readable, the long sometime offers more settings and better 
	demonstrates what Java objects are used under the hood.

	Alias
	-----
	
	An alias maps one or more virtual recipients to another 
	canonical local recipient address. 
	
	Aliases are mostly useful if the POP3 service is enabled.
		
	When a single alias is mapped to a canonical address:
	alias("jeannie@example.com", "jane@example.com"),
	
	When several alias addresses are mapped to a single canonical address:
	
	setup(AliasMapper, {
			aliases: [
				"john1@example.com",
				"john2@example.com"
			],
			canonical: "john.doe@example.com"
	}),
	
	
	Forward
	-------
	
	Forward lists are used to redistribute incoming mail to one or more  
	other addresses, including remote addresses, without changing the 
	reverse path (except applying SRS if necessary).
	
	Short form:
	forward("games@example.com", "john@example.com", "jane@example.com"),

	More complete form:	
	
	setup(RecipientDestinationPair, {
		recipient: "games@example.com",
		destination: setup(ForwardDestination, {
			members: [
				setup(Member, {
					address: "john@example.com",
					name: "John Doe",
				}),
				setup(Member, {
					address: "jane@example.com",
					name: "Jane Doe",
				})
			]
		})
	}),
	
	
	Mailing list
	------------
	
	Mireka provides very simple mailing lists. This implementation is 
	appropriate for small, internal, closed membership lists. There is no 
	archiving, automatized subscription/unsubscription address, moderation 
	etc.
	
	If the requirements are more complex, then a dedicated list manager 
	application should be used, like SubEthaMail, which can be easily 
	integrated with Mireka or it can be used stand-alone on a subdomain.	
	
	mailingList({
		address: "games@example.com",
		listId: "games.example.com",
		subjectPrefix: "[GAMES]",
		membersOnly: false,
		attachmentsAllowed: true,
		replyToList: true,
		reversePath: "list-owner@example.com",
		membersOnlyMessage: "Only example.com employees can post onto this list.",
		nonMemberSenderValidator: setup(SubjectRegexpValidator, {
			pattern: ".*TICKET-NO.*"
		}),
		
		members: [ 
			setup(ListMember, {
				address: "john@example.com",
				name: "John Doe",
				noDelivery: false
			}),
			setup(ListMember, {
				address: "jane@example.com",
				name: "Jane Doe",
			})
		]
	}),
	
	
	Proxy
	-----
	
	A proxy destination relays the SMTP statements received in a mail 
	session to a backend server step-by-step in real-time. In this way both 
	this server and the backend server is able to reject a mail within the 
	SMTP session, before accepting the responsibility of delivering the 
	mail. This fast-fail behaviour is useful in some situations.  
	
	Proxy mappings should be placed at the end of the list, so (completely) 
	locally handled addresses will not be shadowed accidentally by proxied 
	addresses specified using wildcards.
	
	Short form for proxying a massive amount of addresses:
	
	massProxy(backendServer, [
		// The followings are an example on how to specify the set of 
		// proxied recipient addresses
	
		// Fully specified recipients
		setup(InlineRecipientRegistry, {
			addresses: [
				"john@example.com",
				"jack@example.com"
			]
		}),
		
		// A wildcard recipient, only the local part is specified
		setup(CaseInsensitiveAnyDomainRecipient, {
			localPart: "anonymous"
		}),
		
		// Another wildcard recipient with regular expression
		setup(RegexAddressSpecification, {
			localPartRegex: ".*-bounces-.*",
			remotePart: "lists.example.com"
		}),
		
		// Any postmaster address
		setup(AnyPostmaster),
		
		// Addresses corresponding to local user accounts
		setup(GlobalUsersRecipientSpecification, {
			users: globalUsers
		}),
	]),
	
	Complete form:
	
	setup(RecipientSpecificationDestinationPair, {
		destination: setup(RelayDestination, {
			backendServer: backendServer
		}),
		recipientSpecifications: deepList([
			// The followings are an example on how to specify the set of 
			// proxied recipient addresses
			
			// Fully specified recipients
			setup(InlineRecipientRegistry, {
				addresses: [
					"john@example.com",
					"jack@example.com"
				]
			}),
			
			// A wildcard recipient, only the local part is specified
			setup(CaseInsensitiveAnyDomainRecipient, {
				localPart: "anonymous"
			}),
			
			// Another wildcard recipient with regular expression
			setup(RegexAddressSpecification, {
				localPartRegex: ".*-bounces-.*",
				remotePart: "lists.example.com"
			}),
			
			// Any postmaster address
			setup(AnyPostmaster),
			
			// Addresses corresponding to local user accounts
			setup(GlobalUsersRecipientSpecification, {
					users: globalUsers
			}),
		]),
	}),
	
	
	Other
	-----
	
	There are many other types of recipient specifications and destinations
	not mentioned above, for example wildcard recipient specifications.
	
	References
	==========
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
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
		Postmaster aliases
		
		Specify a - preferably local - mailbox address which will be the 
		alias of both the server-wide "Postmaster" and the domain 
		specific local "Postmaster@..." addresses.
		
		Comment it out if you have created a dedicated postmaster user 
		and mailbox, or if this server proxies postmaster mails to
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

