package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.parser.CharClass.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CiString;
import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.MessageFlag;
import mireka.imap.MessageFlagSet;
import mireka.imap.ParenthesizedList;
import mireka.imap.SequenceSet;
import mireka.imap.Session;
import mireka.imap.UnavailableException;
import mireka.imap.acl.Right;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.CommandParser.Level;
import mireka.imap.parser.CommandSyntaxException;
import mireka.imap.store.Mail;
import mireka.imap.store.MailIterator;
import mireka.imap.update.UnilateralResponseOption;
import mireka.maildata.io.Range;

public class FetchCommand extends UidSubcommand {
    private final Logger logger = LoggerFactory.getLogger(FetchCommand.class);
    private final List<Item> ALL = List.of(new FlagsItem(), new InternalDateItem(), new SizeItem(),
            new EnvelopeItem());
    private final List<Item> FAST = List.of(new FlagsItem(), new InternalDateItem(),
            new SizeItem());
    private final List<Item> FULL = List.of(new FlagsItem(), new InternalDateItem(), new SizeItem(),
            new EnvelopeItem(), new BodyContentItem());

    private SequenceSet sequences;
    private List<Item> items = new ArrayList<>();
    private boolean setsSeen;

    public FetchCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        parser.take(' ');
        sequences = parser.parseSequenceSet();
        parser.take(' ');
        parseFetchAttributes();
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        sequences.uid = uidmode;
        if (uidmode && !containsUid()) {
            items.add(new UidItem());
        }
        boolean containsFlagsItem = containsFlags();
        try (MailIterator it = mailbox().list(sequences)) {
            Mail mail;
            while ((mail = it.next()) != null) {
                boolean seenUpdated = updateSeenFlag(mail);
                ParenthesizedList list = new ParenthesizedList();
                if (!containsFlagsItem && seenUpdated) {
                    new FlagsItem().send(mail, list);
                }
                for (Item item : items) {
                    item.send(mail, list);
                }
                out.respondFetch(mail.seq(), list);
            }
        }
        return OK;
    }

    protected boolean updateSeenFlag(Mail mail) throws UnavailableException {
        if (setsSeen) {
            if (hasAccess(Right.SEEN)) {
                MessageFlagSet flags = new MessageFlagSet();
                flags.addAll(mail.flags());
                flags.add(MessageFlag.SEEN);
                UnilateralResponseOption option = new UnilateralResponseOption();
                option.notForSession = session;
                return mail.setFlags(flags, option);
            }
        }
        return false;
    }

    private boolean containsUid() {
        for (Item item : items) {
            if (item instanceof UidItem) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFlags() {
        for (Item item : items) {
            if (item instanceof FlagsItem) {
                return true;
            }
        }
        return false;
    }

    private void parseFetchAttributes() throws CommandSyntaxException, IOException {
        try (Level l = parser.beginLevel("fetch-atts")) {
            if (parser.next() == '(') {
                parser.take();
                while (true) {
                    parseFetchAttribute();
                    if (parser.isSpace())
                        parser.take();
                    else if (parser.next() == ')') {
                        parser.take();
                        return;
                    } else {
                        throw new CommandSyntaxException(
                                parser.formatCommandException("fetch-att-list"));
                    }
                }
            } else {
                if (peekIfMacro()) {
                    parseMacro();
                } else {
                    parseFetchAttribute();
                }
            }
        }
    }

    private boolean peekIfMacro() throws IOException {
        switch (parser.peekKeyword()) {
        case "ALL":
        case "FULL":
        case "FAST":
            return true;
        default:
            return false;
        }
    }

    private void parseMacro() throws CommandSyntaxException, IOException {
        try (Level l = parser.beginLevel("fetch-macro")) {
            String keyword = parser.parseKeyword("fetch-macro");
            switch (keyword) {
            case "ALL":
                items = ALL;
                return;
            case "FULL":
                items = FULL;
                return;
            case "FAST":
                items = FAST;
                return;
            }
        }
    }

    private void parseFetchAttribute() throws CommandSyntaxException, IOException {
        try (Level l = parser.beginLevel("fetch-att")) {
            String keyword = parser.parseKeyword("fetch-att");
            Item item;
            switch (keyword) {
            case "ENVELOPE":
                item = new EnvelopeItem();
                break;
            case "FLAGS":
                item = new FlagsItem();
                break;
            case "INTERNALDATE":
                item = new InternalDateItem();
                break;
            case "RFC822.SIZE":
                item = new SizeItem();
                break;
            case "UID":
                item = new UidItem();
                break;
            case "BODY":
                if (is('[')) {
                    item = new BodyContentItem();
                    setsSeen = true;
                } else
                    item = new BodySimpleStructureItem();
                break;
            case "BODY.PEEK":
                item = new BodyPeekItem();
                break;
            case "BODYSTRUCTURE":
                item = new BodyStructureItem();
                break;
            case "RFC822":
                // this is a deprecated keyword, but Thunderbird uses it as of 2022-02
                item = new Rfc822Item();
                break;
            case "RFC822.PEEK":
                // this is a deprecated keyword, but Thunderbird uses it as of 2022-02
                item = new Rfc822PeekItem();
                break;
            case "RFC822.HEADER":
                // this is a deprecated keyword, but Thunderbird uses it as of 2022-02
                item = new Rfc822HeaderItem();
                break;
            case "BINARY":
                item = new BinaryItem();
                setsSeen = true;
                break;
            case "BINARY.PEEK":
                item = new BinaryPeekItem();
                break;
            case "BINARY.SIZE":
                item = new BinarySizeItem();
                break;
            default:
                throw new CommandSyntaxException(parser.formatKeywordException("fetch-att", l));
            }
            item.parseDetails();
            items.add(item);
        }
    }

    private List<Long> parseSectionPart() throws CommandSyntaxException, IOException {
        try (Level l = parser.beginLevel("section-part")) {
            List<Long> r = new ArrayList<>();
            r.add(parser.parseNzNumber());
            while (peekIfSectionPartNumber()) {
                parser.take();
                r.add(parser.parseNzNumber());
            }
            return r;
        }
    }

    private boolean peekIfSectionPartNumber() throws IOException {
        try (Level l = parser.beginPeekLevel("section-part-number")) {
            if (!is(DOT))
                return false;
            take();
            return is(DIGIT_NZ);
        }
    }

    private Partial parsePartial() throws CommandSyntaxException, IOException {
        Partial partial = new Partial();
        take('<');
        partial.offset = parser.parseNumber64();
        take('.');
        partial.length = parser.parseNumber64();
        take('>');
        return partial;
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    private enum SectionText {
        MIME, HEADER, HEADER_FIELDS, HEADER_FIELDS_NOT, TEXT;
    }

    private static abstract class Item {
        public void parseDetails() throws CommandSyntaxException, IOException {
            // do nothing, useful for simple fetch attributes
        }

        public abstract void send(Mail mail, ParenthesizedList list) throws UnavailableException;
    }

    private abstract class SectionItem extends Item {
        /**
         * body part reference
         */
        @Nullable
        List<Long> part;
        @Nullable
        SectionText text;
        /**
         * useful for {@link SectionText#HEADER_FIELDS} and {@link SectionText#HEADER_FIELDS_NOT}
         */
        List<String> headerList;

        @Nullable
        Partial partial;

        @Override
        public void parseDetails() throws CommandSyntaxException, IOException {
            parseSection();
            if (next() == '<')
                partial = parsePartial();
        }

        private void parseSection() throws CommandSyntaxException, IOException {
            try (Level l = parser.beginLevel("section")) {
                parser.take(parser.next() == '[', "opening bracket");
                if (parser.next() != ']') {
                    parseSectionSpec();
                }
                parser.take(parser.next() == ']', "close bracket");
            }
        }

        private void parseSectionSpec() throws CommandSyntaxException, IOException {
            try (Level l = parser.beginLevel("section-spec")) {
                if (parser.isAlpha()) {
                    parseSectionMsgtext();
                } else if (parser.isDigitNz()) {
                    parseSectionPart();
                    if (parser.next() == '.') {
                        parser.take();
                        parseSectionText();
                    }
                } else
                    throw new CommandSyntaxException(parser.formatCommandException("section-spec"));

            }
        }

        private void parseSectionMsgtext() throws CommandSyntaxException, IOException {
            String keyword = parser.parseKeyword("section-msgtext");
            switch (keyword) {
            case "HEADER":
                text = SectionText.HEADER;
                break;
            case "HEADER.FIELDS":
                text = SectionText.HEADER_FIELDS;
                parser.take(parser.isSpace(), "space");
                headerList = parseHeaderList();
                break;
            case "HEADER.FIELDS.NOT":
                text = SectionText.HEADER_FIELDS_NOT;
                parser.take(parser.isSpace(), "space");
                headerList = parseHeaderList();
                break;
            case "TEXT":
                text = SectionText.TEXT;
                break;
            default:
                throw new CommandSyntaxException(parser.formatCommandException("section-msgtext"));
            }
        }

        private List<String> parseHeaderList() throws CommandSyntaxException, IOException {
            List<String> r = new ArrayList<>();
            parser.take(parser.next() == '(', "header-list");
            r.add(parser.parseAstring("header-fld-name"));
            while (parser.isSpace()) {
                parser.take();
                r.add(parser.parseAstring("header-fld-name"));
            }
            parser.take(parser.next() == ')', "header-list-end");
            return r;
        }

        private void parseSectionText() throws CommandSyntaxException, IOException {
            if (parser.peekKeyword().equals("MIME")) {
                parser.parseKeyword("section-text/MIME");
                text = SectionText.MIME;
            } else {
                parseSectionMsgtext();
            }
        }

    }

    private abstract class BinaryPartItem extends Item {
        @Nullable
        List<Long> part;

        @Override
        public void parseDetails() throws CommandSyntaxException, IOException {
            try (Level l = parser.beginLevel("section-binary")) {
                parser.take('[');
                if (parser.next() != ']') {
                    part = parseSectionPart();
                }
                parser.take(']');
            }
        }

    }

    private abstract class BinaryPartialPartItem extends BinaryPartItem {
        @Nullable
        Partial partial;

        @Override
        public void parseDetails() throws CommandSyntaxException, IOException {
            super.parseDetails();
            if (is('<'))
                parsePartial();
        }
    }

    private static class Partial {
        long offset;
        long length;
    }

    private class EnvelopeItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }
    }

    private class FlagsItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            List<String> flags = new ArrayList<>();
            for (CiString flag : mail.flags()) {
                flags.add(flag.toString());
            }
            list.addNamedKeywordList("FLAGS", flags);
        }
    }

    private class InternalDateItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }

    }

    private class SizeItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            list.addNamedAstring("RFC822.SIZE", Long.toUnsignedString(mail.charsize()));
        }

    }

    private class UidItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            list.addNamedAstring("UID", Long.toUnsignedString(mail.uid()));
        }
    }

    /** BODY keyword without arguments */
    private class BodySimpleStructureItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }

    }

    /** BODY with section specification */
    private class BodyContentItem extends SectionItem {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }
    }

    private class BodyPeekItem extends SectionItem {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }

    }

    private class BodyStructureItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }

    }

    /**
     * This item is deprecated.
     * 
     * Functionally equivalent to BODY[], differing in the syntax of the resulting untagged FETCH
     * data (RFC822 is returned).
     */
    public class Rfc822Item extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) throws UnavailableException {
            list.addNamedNstring("RFC822", mail.body(), mail.charsize());
        }
    }

    /**
     * This item is deprecated.
     * 
     * Functionally equivalent to BODY.PEEK[].
     */
    public class Rfc822PeekItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) throws UnavailableException {
            list.addNamedNstring("RFC822", mail.body(), mail.charsize());
        }
    }

    /**
     * This item is deprecated.
     * 
     * Functionally equivalent to BODY.PEEK[HEADER], differing in the syntax of the resulting
     * untagged FETCH data (RFC822.HEADER is returned).
     */
    public class Rfc822HeaderItem extends Item {

        @Override
        public void send(Mail mail, ParenthesizedList list) throws UnavailableException {
            Range range = mail.maildata().headerAndSeparatorRange();
            list.addNamedNstring("RFC822.HEADER", mail.body(range), range.length);
        }

    }

    private class BinaryItem extends BinaryPartialPartItem {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }
    }

    private class BinaryPeekItem extends BinaryPartialPartItem {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }

    }

    private class BinarySizeItem extends BinaryPartItem {

        @Override
        public void send(Mail mail, ParenthesizedList list) {
            // TODO Auto-generated method stub

        }

    }

}
