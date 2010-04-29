package mireka.transmission.queue;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.transmission.Mail;
import mireka.transmission.queue.dataprop.DataProperties;
import mireka.transmission.queue.dataprop.StringToElementConverter;

class MailEnvelopePersister {

    public DataProperties saveToProperties(Mail mail) {
        DataProperties props = new DataProperties();
        storeMailFieldsIntoProperties(mail, props);
        return props;
    }

    void storeMailFieldsIntoProperties(Mail mail, DataProperties props) {
        props.setString("from", mail.from);
        props.setList("recipients", mail.recipients);
        props.setDate("arrivalDate", mail.arrivalDate);
        props.setString("receivedFromMtaName", mail.receivedFromMtaName);
        props.setInetAddress("receivedFromMtaAddress",
                mail.receivedFromMtaAddress);
        props.setDate("scheduleDate", mail.scheduleDate);
        props.setInt("deliveryAttempts", mail.deliveryAttempts);
    }

    public Mail readFromProperties(DataProperties props) {
        Mail mail = new Mail();
        mail.from = props.getString("from");
        mail.recipients =
                props.getList("recipients",
                        new StringToElementConverter<Recipient>() {

                            @Override
                            public Recipient toElement(String s) {
                                return new MailAddressFactory()
                                        .createRecipientAlreadyVerified(s);
                            }

                        });
        mail.arrivalDate = props.getDate("arrivalDate");
        mail.receivedFromMtaName = props.getString("receivedFromMtaName");
        mail.receivedFromMtaAddress =
                props.getInetAddress("receivedFromMtaAddress");
        mail.scheduleDate = props.getDate("scheduleDate");
        mail.deliveryAttempts = props.getInt("deliveryAttempts");
        return mail;
    }

}
