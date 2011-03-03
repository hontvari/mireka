package mireka.login;

import mireka.pop.PrincipalMaildropTable;

/**
 * This class maps a {@link Principal} to maildrop name, so that the maildrop
 * name is the same as the name of the user principal.
 */
public class GlobalUsersPrincipalMaildropTable implements
        PrincipalMaildropTable {

    @Override
    public String lookupMaildropName(Principal principal) {
        return principal.getName();
    }
}
