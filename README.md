mireka
======

Mireka is a mail server with SMTP, Mail Submission and POP3 services. It is also an SMTP proxy. As a proxy, it can help to prevent or diagnose mail problems, like outgoing backscatter spam.

Homepage
--------

[mireka.org](http://mireka.org)

Features
--------

 * detailed logging
 * basic mail traffic statistics
 * filtering by DNSBL 
 * filtering by SPF
 * accepting mail only for local domains and recipients
 * wildcards (regular expressions) can be used to specify local recipients
 * basic tarpit, to prevent e-mail address harvesting
 * loop detection
 * configurable maximum message size
 * easy implementation and installation of custom filters written in Java
 * separate Message Transfer Agent and Message Submission Agent ports: 25 and 587 by default
 * proxy functionality for incoming mails (both MSA and MTA)
 * the proxy can select from more than one backend server, based on the recipient 
 * standalone (non-proxy) Message Submission Agent implementation with file system based mail queues
 * submission port authentication by IP address or by username-password pair through SMTP authentication
 * POP3 service for retrieving mail, file system based mail store
 * aliases, forward lists and (very) simple mail lists
 * Sender Rewriting Scheme (SRS) for SPF compatible forwarding
 * secure communication using STARTTLS on all services
 * "delayed" DSN reports on temporary failures
 * embeddable
 * runs equally well on any OS, where Java is available: Windows, Linux etc.
 
How it works in proxy mode:

Mireka can proxy both incoming and outgoing SMTP connections.
It accepts an SMTP connection, logs communication between client and server, runs filters in various stages of the mail transaction, and sends the mail to a back-end SMTP server. It relays steps of the mail transaction immediately, without queuing the mail. In this way the back-end server can also reject recipients and message content before accepting irrevocable responsibility for delivery. Proxy mode works for both outgoing and incoming mail.

Any number of ports can be configured, and the proxy and standalone modes can be mixed. For example incoming mails can be proxied on one port, while outgoing mails received on another port are transmitted directly by Mireka alone.

Installation
------------

To run it once: 
* install a Java JRE
* download the binary archive mireka-n.n.n.zip from the [GitHub Releases page](https://github.com/hontvari/mireka/releases) and extract it
* run bin/start.sh or bin/start.bat. Note: the default configuration uses the standard mail ports which are below 1024, and those are only allowed for root on *nix. Run it as root or reconfigure ports or use authbind.

See [Quick start for Linux](http://mireka.org/doc/quick-start-linux.html) and [Quick start for Windows](http://mireka.org/doc/quick-start-windows.html) for more information.

For a real installation use the deb package for Ubuntu. For other OS download the binary archive and follow the Installation sections of the [documentation](http://mireka.org/doc/).

Where to get help
-----------------

 * [Documentation](http://mireka.googlecode.com/svn/doc/index.html) and  [Javadoc](http://mireka.googlecode.com/svn/doc/javadoc/index.html)
 * [Mailing list](http://groups.google.com/group/mireka)

Current usage
-------------

Mireka was used as a proxy in front of two [Apache James Servers](http://james.apache.org/server/index.html) receiving 50.000 mail transactions daily. Now it is used both as full mail server and a send only mail server with similar traffic and also as a relay server.

History
-------

Previously I contributed a few patch to Apache James, but after a while I had to start to maintain my own branch. That was difficult, so when I needed more feature, I decided to implement it as a separate process, in the form of a proxy, instead of a set of patches. So Mireka was born as a simple proxy, which provided fast fail checks. Features were slowly added and eventually the proxy become a simple, but complete standalone mail server.

Contributors
------------

Hontvári Levente - I am a Java server side developer and an accidental sysadmin. I work for  [FlyOrDie.com](http://www.flyordie.com). (Levente is my given name.) 

Credits
-------

Mireka is written in Java. A large part of the functionality is provided by components produced by other projects: 
 * [SubEthaSMTP](http://code.google.com/p/subethasmtp/) for receiving mails
 * [Logback](http://logback.qos.ch/) for logging
 * [Mime4J](http://james.apache.org/mime4j/) for parsing message content and constructing DSN messages
 * [dnsjava](http://www.dnsjava.org/) for querying DNSBLs and determining the SMTP servers of recipient domains
 * [jSPF](http://james.apache.org/jspf/) for checking the sender

License
-------

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

