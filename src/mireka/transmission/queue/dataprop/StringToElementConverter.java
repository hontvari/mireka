package mireka.transmission.queue.dataprop;

public interface StringToElementConverter<T> {
    T toElement(String s);
}