/* 
	List of IP addresses of hosts which are allowed to send mails 
	via the submission service without authentication.
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/

[
	/*
	new IpAddress("192.0.2.128"),
	new IpAddress("192.0.2.0/28")
	*/
	
	// IPv4 loopback range
	new IpAddress("127.0.0.0/8"),
	// IPv6 loopback address
	new IpAddress("::1"),	
]
