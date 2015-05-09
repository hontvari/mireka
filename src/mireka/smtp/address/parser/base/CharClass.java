package mireka.smtp.address.parser.base;

public interface CharClass {
    boolean isSatisfiedBy(int ch);

    /**
     * Returns a readable description of this character class, for example
     * "letters".
     */
    String toString();

}
