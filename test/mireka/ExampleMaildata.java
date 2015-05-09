package mireka;

import mireka.maildata.Maildata;

public class ExampleMaildata {
    public static Maildata simple() {
        return new Maildata(ExampleMaildataFile.simple());
    }

    /**
     * loads a mail from a file on the class path
     * 
     * @param caller
     *            it gives the base package, if a relative name is supplied
     * @param name
     *            either an absolute or a relative name, for example /mail.eml
     */
    public static Maildata fromResource(Class<?> caller, String name) {
        return new Maildata(ExampleMaildataFile.fromResource(caller, name));
    }

    public static Maildata mail4k() {
        return new Maildata(ExampleMaildataFile.mail4k());
    }
}
