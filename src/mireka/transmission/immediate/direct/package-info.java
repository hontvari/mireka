/**
 * An ImmediateSender implementation package, which makes a single synchronous 
 * attempt to directly transmits mail to the SMTP servers of the remote domain. 
 * The remote domain is specified by the remote part of the recipient addresses, 
 * which must be the same for all recipients in case of this implementation. The 
 * receiving SMTP servers are usually specified by the MX records of the remote 
 * domain, except if the remote part is a literal address, or the domain has an
 * implicit MX record only. 
 */
package mireka.transmission.immediate.direct;