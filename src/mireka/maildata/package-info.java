/**
 * Internet Message Format parser and generator for parsing and generating the 
 * message content (also known as Mail Data). 
 * 
 * Electronic mail consists of an envelope and content. This package deals 
 * exclusively with the content. This distinction is important. There are many 
 * identical terms which are used in both the content and the envelope context, 
 * but their definition is slightly different. These similarities and 
 * differences appear in the syntactic grammar of the message content and the 
 * envelope. For example a string which correctly specifies an email address 
 * from the viewpoint of this package may be invalid in the context of SMTP.
 * 
 * Internet Message Format as of RFC 5322 is composed of US-ASCII characters. 
 * RFC 6854 made it possible to represent non-ASCII text in header fields, for 
 * example in Subject, using the encoded-word mechanism, which still assures 
 * that mail data is ASCII only. Where not mentioned otherwise, String values 
 * which are the result of parsing or the input to generation are ASCII only. 
 * It is explicitly documented if one of those String values may contain 
 * non-ASCII characters.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet Message Format</a>
 * @see <a href="https://tools.ietf.org/html/rfc2047">RFC 2047 - MIME (Multipurpose Internet Mail Extensions) Part Three: Message Header Extensions for Non-ASCII Text</a>
 * @see <a href="https://tools.ietf.org/html/rfc6854">RFC 6854 - Update to Internet Message Format to Allow Group Syntax in the "From:" and "Sender:" Header Fields</a>
 * @see <a href="https://tools.ietf.org/html/rfc2231">RFC 2231 - MIME Parameter Value and Encoded Word Extensions: Character Sets, Languages, and Continuations
 */
package mireka.maildata;