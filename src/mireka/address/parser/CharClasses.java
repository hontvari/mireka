package mireka.address.parser;

import mireka.address.parser.base.CharClass;

public class CharClasses {
    public static final CharClass ALPHA = new CharClass() {

        @Override
        public boolean isSatisfiedBy(int ch) {
            return (0x41 <= ch && ch <= 0x5A) || (0x61 <= ch && ch <= 0x7A);
        }

        @Override
        public String toString() {
            return "letter";
        }
    };
    public static final CharClass DIGIT = new CharClass() {

        @Override
        public boolean isSatisfiedBy(int ch) {
            return 0x30 <= ch && ch <= 0x39;
        }

        @Override
        public String toString() {
            return "digit";
        }
    };

    /**
     * Letter, digit
     */
    public static final CharClass LET_DIG = new CharClass() {

        @Override
        public boolean isSatisfiedBy(int ch) {
            return ALPHA.isSatisfiedBy(ch) || DIGIT.isSatisfiedBy(ch);
        }

        @Override
        public String toString() {
            return "letter or digit";
        }
    };

    /**
     * Letter, digit, hyphen.
     */
    public static final CharClass LDH = new CharClass() {

        @Override
        public boolean isSatisfiedBy(int ch) {
            return ALPHA.isSatisfiedBy(ch) || DIGIT.isSatisfiedBy(ch)
                    || ch == '-';
        }

        @Override
        public String toString() {
            return "letter, digit or hyphen";
        }
    };

    static final CharClass HEX = new CharClass() {
    
        @Override
        public boolean isSatisfiedBy(int ch) {
            return ('0' <= ch && ch <= '9') || ('a' <= ch && ch <= 'f')
                    || ('A' <= ch && ch <= 'F');
        }
    
        @Override
        public String toString() {
            return "hex digit";
        };
    };

}