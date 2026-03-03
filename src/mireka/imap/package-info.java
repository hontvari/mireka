/**
 * Contains an IMAP server.
 * 
 * IMAP means Internet Message Access Protocol, it is used by email clients to retrieve email
 * messages from the mail server. In contrast to POP, an IMAP mailbox can be used concurrently by
 * multiple email clients. Clients generally leave messages on the server until the user explicitly
 * deletes them.
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9051">Internet Message Access Protocol (IMAP) - Version 4rev2</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc2683">IMAP4 Implementation Recommendations</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc2062">Internet Message Access Protocol - Obsolete Syntax</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc2061">IMAP4 COMPATIBILITY WITH IMAP2BIS</a>
 */
package mireka.imap;