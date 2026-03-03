package mireka;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class Multilang {
    private static final ConcurrentHashMap<Locale, Multilang> instances = new ConcurrentHashMap<>();
    
    public final Locale locale;

    public static Multilang get(Locale locale) {
        return instances.computeIfAbsent(locale, Multilang::new);
    }

    private Multilang(Locale locale) {
        this.locale = locale;
    }

    private String get(String id) {
        return ResourceBundle.getBundle("messages", locale).getString(id);
    }

    public String sieveScriptSubject() {
        return get("sieveScriptSubject");
    }

}
