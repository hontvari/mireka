/**
 * Internet Message Format parser and generator for parsing and generating the 
 * message content (also known as Mail Data). Electronic mail consists of an 
 * envelope and content. This package deals exclusively with the content. This 
 * distinction is important. There are many identical terms which are used in 
 * both the content and the envelope context, but their definition is slightly
 * different. These similarities and differences appear in the syntactic 
 * grammar of the message content and the envelope. For example a string which 
 * correctly specifies an email address from the viewpoint of this package may 
 * be invalid in the context of SMTP.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet Message Format</a>
 * @see <a href="https://tools.ietf.org/html/rfc2047">RFC 2047 - MIME (Multipurpose Internet Mail Extensions) Part Three: Message Header Extensions for Non-ASCII Text</a>
 */
package mireka.maildata;