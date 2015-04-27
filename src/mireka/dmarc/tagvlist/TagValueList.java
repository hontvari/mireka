package mireka.dmarc.tagvlist;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagValueList {

    public List<TagSpec> list = new ArrayList<>();
    private Map<String, String> map = new HashMap<>();

    public void addAll(List<TagSpec> tagList) throws ParseException {
        for (TagSpec tagSpec : tagList) {
            this.list.add(tagSpec);
            String existing = map.put(tagSpec.name, tagSpec.value);
            if (existing != null) {
                throw new ParseException("Duplicate tag name: " + tagSpec.name,
                        0);
            }
        }
    }

    public String get(String name) {
        return map.get(name);
    }
}
