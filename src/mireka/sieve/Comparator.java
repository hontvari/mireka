package mireka.sieve;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Comparator {

    CompiledKey compileKey(String key, Kind operation);

    CompiledValue compileValue(String value, Kind operation);

    boolean is(CompiledKey key, CompiledValue value);

    boolean contains(CompiledKey key, CompiledValue value);

    boolean match(CompiledKey key, CompiledValue value);

    public static interface CompiledKey {
    }
    
    public static interface CompiledValue {
    }

    public static class CompiledString implements CompiledValue, CompiledKey {
        public String value;

        public CompiledString(String value) {
            this.value = value;
        }
    }

    public static class PatternKey implements CompiledKey {
        public Pattern pattern;

        public PatternKey(Pattern pattern) {
            this.pattern = pattern;
        }
    }

    public static class RegexpComparator implements Comparator {
        private static final Logger logger = LoggerFactory
                .getLogger(Comparator.RegexpComparator.class);
        private final ComparatorKind kind;

        public RegexpComparator(ComparatorKind kind) {
            this.kind = kind;
        }

        @Override
        public CompiledKey compileKey(String key, Kind operation) {
            int flags = 0;
            switch (kind) {
            case OCTET:
                break;
            case ASCII_CASEMAP:
                flags = Pattern.CASE_INSENSITIVE;
                break;
            case UNICODE_CASEMAP:
                flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
                break;
            }
            key = Normalizer.normalize(key, Form.NFKD);
            String pattern;
            switch (operation) {
            case Is:
            case Contains:
                pattern = key;
                flags |= Pattern.LITERAL;
                break;
            case Matches:
                pattern = compilePattern(key);
                break;
            default:
                throw new AssertionError();
            }
            return new PatternKey(Pattern.compile(pattern, flags));
        }

        @Override
        public CompiledValue compileValue(String value, Kind operation) {
            return new CompiledString(Normalizer.normalize(value, Form.NFKD));
        }

        @Override
        public boolean is(CompiledKey key, CompiledValue value) {
            Pattern k = ((PatternKey) key).pattern;
            String v = ((CompiledString) value).value;
            return k.matcher(v).matches();
        }

        @Override
        public boolean contains(CompiledKey key, CompiledValue value) {
            Pattern k = ((PatternKey) key).pattern;
            String v = ((CompiledString) value).value;
            return k.matcher(v).find();
        }

        @Override
        public boolean match(CompiledKey key, CompiledValue value) {
            Pattern k = ((PatternKey) key).pattern;
            String v = ((CompiledString) value).value;
            return k.matcher(v).matches();
        }
        
        private static String compilePattern(String key) {
            QuotingBuilder p = new QuotingBuilder();
            CharScanner s = new CharScanner(key);
            while (!s.isEof()) {
                if (s.is('\\')) {
                    s.takeIt();
                    if (s.is('?') || s.is('*')) {
                        p.appendLiteral((char) s.next);
                    } else if (s.isEof()) {
                        p.appendLiteral('\\');
                    } else {
                        p.appendLiteral('\\');
                        p.appendLiteral((char) s.next);
                    }
                } else if (s.is('?')) {
                    p.appendMeta('.');
                } else if (s.is('*')) {
                    p.appendMeta('.');
                    p.appendMeta('*');
                } else {
                    p.appendLiteral((char) s.next);
                }
                s.takeIt();
            }
            logger.trace("Compiled pattern: {}", p.b);
            return p.b.toString();
        }

        private static class QuotingBuilder {
            public StringBuilder b = new StringBuilder();
            boolean quoting = false;

            public void appendLiteral(char c) {
                if (quoting || harmless(c)) {
                    b.append(c);
                } else {
                    b.append("\\Q");
                    b.append(c);
                    quoting = true;
                }
            }
            
            public void appendMeta(char c) {
                if (quoting) {
                    b.append("\\E");
                    quoting = false;
                }
                b.append(c);
            }

            private boolean harmless(char c) {
                return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || '0' <= c && c <= '9'
                        || c >= 128 || c == ' ' || c == '@' || c == '-';
            }

        }
    }
}
