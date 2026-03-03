package mireka.imap;

import java.util.Locale;
import java.util.Objects;

public class CiString implements CharSequence {
    public String original;
    public String upperCase;
    public String lowerCase;

    public CiString(String original) {
        this.original = original;
        /**
         * Uppercase is preferred compared to lowercase
         */
        this.upperCase = original.toUpperCase(Locale.US);
        this.lowerCase = original.toLowerCase(Locale.US);
    }

    @Override
    public String toString() {
        return original;
    }

    @Override
    public int hashCode() {
        return Objects.hash(upperCase);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CiString other = (CiString) obj;
        return Objects.equals(upperCase, other.upperCase);
    }

    @Override
    public int length() {
        return original.length();
    }

    @Override
    public char charAt(int index) {
        return original.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return original.subSequence(start, end);
    }
}