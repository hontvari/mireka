package mireka.imap.acl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ACL {
    public List<Element> elements = new ArrayList<>();

    public EnumSet<Right> rights(Set<AccessIdentifier> identifiers) {
        EnumSet<Right> rights = EnumSet.noneOf(Right.class);
        for (Element element : elements) {
            if (identifiers.contains(element.identifier)) {
                if (element.isNegative)
                    rights.removeAll(element.rights);
                else
                    rights.addAll(element.rights);
            }
        }
        return rights;
    }

    /**
     * Used for configuration
     */
    public void add(String identifier, String rights) {
        Element element = new Element();
        element.configure(identifier, rights);
        elements.add(element);
    }

    public static class Element {
        public boolean isNegative;
        public AccessIdentifier identifier;
        public EnumSet<Right> rights = EnumSet.noneOf(Right.class);

        public void configure(String identifier, String rights) {
            if (identifier.startsWith("-")) {
                identifier = identifier.substring(1);
                isNegative = true;
            }
            this.identifier = AccessIdentifier.from(identifier);
            for (char ch : rights.toCharArray()) {
                this.rights.add(Right.forLetter(ch));
            }
        }

        @Override
        public String toString() {
            return "<" + (isNegative ? "-" : "") + identifier + ": " + Right.toString(rights) + ">";
        }
    }

    @Override
    public String toString() {
        return elements.stream().map(Element::toString).collect(Collectors.joining(", "));
    }

}
