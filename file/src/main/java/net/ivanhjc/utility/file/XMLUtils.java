package net.ivanhjc.utility.file;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class XMLUtils {

    /**
     * java bean转xml
     *
     * @param objectClass 如:A.class
     * @param object 如：A a
     * @return
     * @throws JAXBException
     */
    public static String beanToXml(Class objectClass, Object object) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(objectClass);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        String xml = writer.toString();
        return xml;
    }

    /**
     * xml转java bean
     * @param xml
     * @param objectClass 如:A.class
     * @return
     * @throws JAXBException
     */
    public static Object xmlToBean(String xml, Class objectClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(objectClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object object = unmarshaller.unmarshal(new StringReader(xml));
        return object;
    }
}
