/*
	Forward lists are used to redistribute incoming mail to multiple 
	other addresses.
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/
forwards = setup(ForwardLists, { lists: [
		
	/*
	setup(ForwardList, {
		address: "games@example.com",
		transmitter: primaryTransmitter,
		srs: srs,
		
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
	*/
		
]});
	
	