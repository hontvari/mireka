package mireka.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import mireka.imap.parser.Generator;

/**
 * ParanthesizedList is a generic data structure, see RFC 9051 Paranthesized List, see
 * tagged-ext-val syntax.
 */
public class ParenthesizedList {
    public Collection<Value> elements = new ArrayList<>();

    public ParenthesizedList addNamedAstring(String name, String value) {
        elements.add(new AtomValue(name));
        elements.add(new AstringValue(value));
        return this;
    }
    
    public ParenthesizedList addNamedKeywordList(String name, Collection<String> values) {
        elements.add(new AtomValue(name));
        elements.add(new KeywordListValue(values));
        return this;
    }

    public ParenthesizedList addNamedNstring(String name, InputStream value, long length) {
        elements.add(new AtomValue(name));
        elements.add(new StreamNstringValue(value, length));
        return this;
    }

    public void generate(Generator out) throws IOException {
        boolean empty = true;
        out.write('(');
        for (Value element : elements) {
            if (empty) {
                empty = false;
            } else {
                out.write(' ');
            }
            element.generate(out);
        }
        out.write(')');
    }

    public static abstract class Value {
        public abstract void generate(Generator out) throws IOException;
    }

    public static class NamedValue extends Value {
        String tag;
        Value value;

        public NamedValue(String tag, Value value) {
            this.tag = tag;
            this.value = value;
        }
        
        @Override
        public void generate(Generator out) throws IOException {
            out.write(tag);
            out.write(' ');
            value.generate(out);
        }
    }

    public static class AstringValue extends Value {
        String astring;

        AstringValue(String value) {
            this.astring = value;
        }

        @Override
        public void generate(Generator out) throws IOException {
            out.writeAstring(astring);
        }
    }

    public static class AtomValue extends Value {
        String atom;

        AtomValue(String atom) {
            this.atom = atom;
        }

        @Override
        public void generate(Generator out) throws IOException {
            out.writeAtom(atom);
        }
    }

    public static class KeywordValue extends Value {
        String keyword;

        KeywordValue(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public void generate(Generator out) throws IOException {
            out.write(keyword);
        }
    }

    public static class KeywordListValue extends Value {
        Collection<String> keywords;

        KeywordListValue(Collection<String> keywords) {
            this.keywords = keywords;
        }

        @Override
        public void generate(Generator out) throws IOException {
            boolean empty = true;
            out.write('(');
            for (String keyword : keywords) {
                if (empty) {
                    empty = false;
                } else {
                    out.write(' ');
                }
                out.write(keyword);
            }
            out.write(')');
        }
    }
    
    private static class StreamNstringValue extends Value {

        private InputStream value;
        private long length;

        public StreamNstringValue(InputStream value, long length) {
            this.value = value;
            this.length = length;
        }

        @Override
        public void generate(Generator out) throws IOException {
            out.writeNstring(value, length);
        }
    }
}
