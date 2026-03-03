/*
	List of users. These users can authenticate with the Mail Submission, POP3,
	IMAP using the authentication information supplied here.
	
	Note: Because this file may contain clear text passwords it should be 
	protected, i.e. it should not be readable by anyone.
	
	Example:
	globalUser("john", "changeit"),
*/
users = [
	setup(User, { username: "john", password: "CHANGEIT" }),


];


