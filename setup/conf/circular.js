/*
	Unitialized objects are created here in order to resolve 
	circular references.
*/
primaryTransmitter = new QueuingTransmitter;
dsnTransmitter = new QueuingTransmitter;
retryTransmitter = new QueuingTransmitter;