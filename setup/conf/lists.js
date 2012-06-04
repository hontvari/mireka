/*
	Simple mailing lists. This implementation is appropriate for small, 
	internal, closed membership lists. There is no archiving,  
	automatized subscription/unsubscription address, moderation etc.
	If the requirements are more complex, then a dedicated list manager 
	application should be used, like SubEthaMail, which can be easily 
	integrated with Mireka or it can be used stand-alone on a subdomain.
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/
lists = [

	/*	
	setupList({
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
	*/
		
];	