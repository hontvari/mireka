/* 
	Configuration of the persistent mail queues used by the SMTP Mail 
	Submission service.
	
	For more information on the individual configuration elements see the 
	Javadoc documentation in the doc/javadoc directory. For an overview read
	the documentation in the doc directory.  
*/

/*
	Mail address used in the From header of Message Delivery 
	Notification messages, sent by Mireka to notify the sender about
	a failed delivery.
*/
mailerdaemon = setup(NameAddr, {
	displayName: "Mail Delivery Subsystem", 
	addressSpec: "mailer-daemon@example.com"
}); 

submittedMailQueue = setup(ScheduleFileDirQueue, {
	store: setup(FileDirStore, {
		dir: "queues/submitted"
	}),
	mailProcessorFactory: primaryTransmitter,
	threadCount: 10,
});

retryMailQueue = setup(ScheduleFileDirQueue, {
	store: setup(FileDirStore, {
		dir: "queues/retry"
	}),
	mailProcessorFactory: retryTransmitter,
	threadCount: 5,
});

dsnMailQueue = setup(ScheduleFileDirQueue, {
	store: setup(FileDirStore, {
		dir: "queues/dsn"
	}),
	mailProcessorFactory: dsnTransmitter,
	threadCount: 5,
});

logIdFactory = setup(LogIdFactory);

outgoingConnectionRegisty = setup(OutgoingConnectionsRegistry, {
	/* 
		uncomment to switch off limiting simultaneous connections 
		to a single host
	*/
	/*
	maxConnectionsToHost: 0,
	*/		
});

mailToHostTransmitterFactory = setup(MailToHostTransmitterFactory, {
	clientFactory: clientFactory,
	outgoingConnectionRegistry: outgoingConnectionRegisty,
	logIdFactory: logIdFactory,
});

immediateSenderFactory = setup(ImmediateSenderFactory, {
	mailToHostTransmitterFactory: mailToHostTransmitterFactory
});

dsnMailCreator = setup(DsnMailCreator, {
	reportingMtaName: helo, 
	fromAddress: mailerdaemon
});

retryPolicy = setup(RetryPolicy, {
	/* count of attempts after a Delayed DSN mail must be sent */
	/*
	delayReportPoint: 3,
	*/
	dsnMailCreator: dsnMailCreator,
	dsnTransmitter: dsnTransmitter,
	retryTransmitter: retryTransmitter,
});

setup(primaryTransmitter, {
	queue: submittedMailQueue,
	immediateSenderFactory: immediateSenderFactory,
	retryPolicy: retryPolicy,
	logIdFactory: logIdFactory,
	summary: setup(TransmitterSummary, {
		name: "submission",
	}),
});

setup(dsnTransmitter, {
	queue: dsnMailQueue,
	immediateSenderFactory: immediateSenderFactory,
	retryPolicy: retryPolicy,
	logIdFactory: logIdFactory,
	summary: setup(TransmitterSummary, {
		name: "dsn",
	}),
});

setup(retryTransmitter, {
	queue: retryMailQueue,
	immediateSenderFactory: immediateSenderFactory,
	retryPolicy: retryPolicy,
	logIdFactory: logIdFactory,
	summary: setup(TransmitterSummary, {
		name: "retry",
	}),
});

