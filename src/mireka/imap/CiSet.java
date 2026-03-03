package mireka.imap;

import java.util.AbstractSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Case insensitive set, some elements can have a canonical case, which is always returned.
 */
public class CiSet<T extends Enum<?>> extends AbstractSet<CiString> {
    private final Set<CiString> set = new HashSet<>();
    private final Map<CiString, CiString> keywords = new HashMap<>();

    public CiSet(String... keyword) {
        for (String w : keyword) {
            CiString ciw = new CiString(w);
            keywords.put(ciw, ciw);
        }
    }

    public <E extends Enum<E>> CiSet(Class<E> clazz) {
        this(EnumSet.allOf(clazz).stream().map(String::valueOf).toArray(String[]::new));
    }

    public boolean add(String v) {
        return add(new CiString(v));
    }

    @Override
    public boolean add(CiString v) {
        if (keywords.containsKey(v))
            v = keywords.get(v);
        return set.add(v);
    }

    public boolean add(T v) {
        return add(v.toString());
    }

    @Override
    public Iterator<CiString> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return set.size();
    }
}
