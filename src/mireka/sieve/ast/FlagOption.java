package mireka.sieve.ast;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import mireka.imap.CiString;
import mireka.imap.MessageFlag;
import mireka.imap.MessageFlagSet;
import mireka.sieve.Context;

public class FlagOption {
    @Nullable
    public List<String> strings = null;

    public MessageFlagSet flags(Context context) {
        if (strings == null)
            return context.messageFlags;
        else {
            return parseFlagNames(strings);
        }
    }

    /**
     * One flag string may contain many whitespace separated flags. Invalid flag values are ignored.
     */
    @SuppressWarnings("resource")
    public static MessageFlagSet parseFlagNames(List<String> strings) {
        List<CiString> flags = strings.stream().flatMap(n -> new Scanner(n).tokens())
                .filter(MessageFlag::validFlag).map(CiString::new).collect(Collectors.toList());
        MessageFlagSet r = new MessageFlagSet();
        r.addAll(flags);
        return r;
    }

}
