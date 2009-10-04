package mireka.mailaddress;

import java.util.Locale;

public class Domain {
    public final String value;
    private final String valueInLowerCase;

    public Domain(String value) {
        this.value = value;
        this.valueInLowerCase = value.toLowerCase(Locale.US);
    }
    
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime
                        * result
                        + ((valueInLowerCase == null) ? 0 : valueInLowerCase
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
        Domain other = (Domain) obj;
        if (valueInLowerCase == null) {
            if (other.valueInLowerCase != null)
                return false;
        } else if (!valueInLowerCase.equals(other.valueInLowerCase))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return value;
    }

}
