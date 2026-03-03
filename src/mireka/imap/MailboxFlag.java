package mireka.imap;

/**
 * Mailbox name attributes.
 * 
 * @see "mbx-list-flags"
 */
public enum MailboxFlag {
    /**
     * Selectability flag, only one per LIST response
     */
    NON_EXISTENT("\\NonExistent"), NO_SELECT("\\Noselect"), MARKED("\\Marked"),
    UNMARKED("\\Unmarked"),

    /**
     * Other flags, multiple from this list are possible per LIST response
     */
    NO_INFERIORS("\\NoInferiors"), HAS_CHILDREN("\\HasChildren"),
    HAS_NO_CHILDREN("\\HasNoChildren"), SUBSCRIBED("\\Subscribed"), REMOTE("\\Remote"),

    /**
     * Role flags, 0, 1 or more per LIST response
     */
    R_ALL("\\All"), R_ARCHIVE("\\Archive"), R_DRAFTS("\\Drafts"), R_FLAGGED("\\Flagged"),
    R_JUNK("\\Junk"), R_SENT("\\Sent"), R_TRASH("\\Trash");

    public String imapName;

    MailboxFlag(String value) {
        this.imapName = value;
    }
}
