/**
 * 
 */
package org.openforis.idm.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;

import org.w3c.dom.Node;

/**
 * @author M. Togna
 * 
 */
public class XmlBindingUtil {

	@SuppressWarnings("unchecked")
	public static <T> T unmarshall(Class<T> clazz, Node node, Unmarshaller.Listener listener) throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller(clazz, listener);
		return (T) unmarshaller.unmarshal(node);
	}

	@SuppressWarnings("unchecked")
	public static <T> T unmarshall(Class<T> clazz, Reader reader, Unmarshaller.Listener listener) throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller(clazz, listener);
		return (T) unmarshaller.unmarshal(reader);
	}

	@SuppressWarnings("unchecked")
	public static <T> T unmarshall(Class<T> clazz, InputStream inputStream, Unmarshaller.Listener listener) throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller(clazz, listener);
		return (T) unmarshaller.unmarshal(inputStream);
	}

	public static <T> T unmarshall(Class<T> clazz, String filename, Unmarshaller.Listener listener) throws JAXBException, IOException {
		FileInputStream is = null;
		T object;
		try {
			is = new FileInputStream(filename);
			object = unmarshall(clazz, is, listener);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return object;
	}

	public static void marshall(Object object, Node root, Marshaller.Listener listener) throws JAXBException {
		Marshaller marshaller = createMarshaller(object.getClass(), listener);
		marshaller.marshal(object, root);
	}

	public static void marshall(Object object, OutputStream os, Marshaller.Listener listener) throws JAXBException {
		Marshaller marshaller = createMarshaller(object.getClass(), listener);
		marshaller.marshal(object, os);
	}

	public static void marshall(Object object, Result result, Marshaller.Listener listener) throws JAXBException {
		Marshaller marshaller = createMarshaller(object.getClass(), listener);
		marshaller.marshal(object, result);
	}

	public static void marshall(Object object, Writer writer, Marshaller.Listener listener) throws JAXBException {
		Marshaller marshaller = createMarshaller(object.getClass(), listener);
		marshaller.marshal(object, writer);
	}

	private static <T> Marshaller createMarshaller(Class<T> clazz, Marshaller.Listener listener) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(clazz);
		Marshaller marshaller = jc.createMarshaller();
		if (listener != null) {
			marshaller.setListener(listener);
		}
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		return marshaller;
	}

	private static <T> Unmarshaller getUnmarshaller(Class<T> clazz, Unmarshaller.Listener listener) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		if (listener != null) {
			unmarshaller.setListener(listener);
		}
		return unmarshaller;
	}
}
