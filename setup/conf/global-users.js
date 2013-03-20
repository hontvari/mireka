/*
	List of global users. These users have a user name which is valid on 
	all local domains and who can authenticate with the Mail Submission 
	service using the authentication information supplied here.
	
	Note: Because this file may contain clear text passwords it should be 
	protected, i.e. it should not be readable by anyone.
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.
	
	Examples
	========
	
	Short form:
	globalUser("john", "changeit"),
	where john is a username, changeit is a password.

	More complete form:
	setup(GlobalUser, {
		username: "john",
		password: "changeit"
	})
	
*/
globalUsers = setup(GlobalUsers, { users: [

	/*
	globalUser("john", "changeit"),
	globalUser("jane", "changeit"),
	*/
	
]});
	