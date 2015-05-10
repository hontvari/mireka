/**
 * This is not a full DMARC implementation, its only purpose to filter mail from 
 * those mail providers (YAHOO), which publish a DMARC policy that
 * breaks proper mail list functionality. Emails from such providers which are 
 * sent to a mail list address must be rejected or mangled. If nothing is done,
 * the mails sent by the mailing list will be bounced by third parties. 
 *  
 * @see <a href="https://tools.ietf.org/html/rfc7489">Informational RFC 7489 - 
 * Domain-based Message Authentication, Reporting, and Conformance (DMARC)</a>
 */
package mireka.dmarc;