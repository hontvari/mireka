/**
 * Contains the filter API; filters follow an SMTP transaction and may respond 
 * to individual SMTP commands, they are arranged into a  chain, the first calls 
 * the second and so on. The functionality of a filter varies from a simple 
 * check to the final delivery of a mail. 
 */
package mireka.filter;

