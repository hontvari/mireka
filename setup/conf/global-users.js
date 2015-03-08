/*
	List of global users. These users have a user name which is valid on 
	all local domains and who can authenticate with the Mail Submission 
	service using the authentication information supplied here.
	
	Note: Because this file may contain clear text passwords it should be 
	protected, i.e. it should not be readable by anyone.
	
	Example:
	globalUser("john", "changeit"),
*/
globalUsers = setup(GlobalUsers, { users: [



]});
