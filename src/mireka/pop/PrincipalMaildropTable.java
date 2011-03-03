package mireka.pop;

import mireka.login.Principal;

/**
 * This interface is used to look up the name of the maildrop assigned to a user
 * principal.
 */
public interface PrincipalMaildropTable {
    public String lookupMaildropName(Principal principal);
}
