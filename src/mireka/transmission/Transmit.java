package mireka.transmission;

import mireka.filter.Destination;

/**
 * The Transmit destination marks a recipient for which the mail must be
 * transmitted asynchronously to a remote MTA as specified by the remote part of
 * the address. Usually the remote part is a domain name, and the MTA must be
 * find by looking up the DNS MX record of that domain.
 */
public class Transmit implements Destination {
    // the class type alone holds enough information
}
