package mireka.login;

import java.util.Locale;

import javax.annotation.concurrent.ThreadSafe;

import mireka.smtp.address.LocalPart;

/**
 * case-insensitive user name, where case-insensitivity is defined by the en_US
 * locale. Note that in a general case case-insensitivity depends on locale, but
 * basic SMTP, without Unicode extensions, only allows ASCII local parts in mail
 * addresses.
 */
@ThreadSafe
public class Username {
    private final String name;
    private final String nameInLowerCase;

    public Username(String name) {
        this.name = name;
        this.nameInLowerCase = name.toLowerCase(Locale.US);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((nameInLowerCase == null) ? 0 : nameInLowerCase
                                .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Username other = (Username) obj;
        if (nameInLowerCase == null) {
            if (other.nameInLowerCase != null)
                return false;
        } else if (!nameInLowerCase.equals(other.nameInLowerCase))
            return false;
        return true;
    }

    public boolean matches(LocalPart localPart) {
        return localPart.displayableName().toLowerCase(Locale.US)
                .equals(nameInLowerCase);
    }
}
