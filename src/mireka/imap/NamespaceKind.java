package mireka.imap;

public enum NamespaceKind {
    PERSONAL(1), OTHER_USERS(2), SHARED(3);

    /**
     * It indicated the conventional order of namespaces, e.g. first are the personal namespaces,
     * etc.
     */
    public int position;

    NamespaceKind(int position) {
        this.position = position;
    }
}