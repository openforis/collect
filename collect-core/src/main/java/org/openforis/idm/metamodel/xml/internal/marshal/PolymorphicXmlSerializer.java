package org.openforis.idm.metamodel.xml.internal.marshal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class PolymorphicXmlSerializer<T, P> extends XmlSerializerSupport<T, P> {

	private Map<Class<? extends T>, XmlSerializerSupport<? extends T, ?>> delegateMarshallers;
	
	public PolymorphicXmlSerializer() {
		this.delegateMarshallers = new HashMap<Class<? extends T>, XmlSerializerSupport<? extends T,?>>();
	}
	
	protected <V extends T> void setDelegate(Class<V> clazz, XmlSerializerSupport<V, ?> delegate) {
		delegateMarshallers.put(clazz, delegate);
	}
	
	@SuppressWarnings("unchecked")
	protected <V extends T> XmlSerializerSupport<V, ?> getDelegate(Class<V> clazz) {
		return (XmlSerializerSupport<V, ?>) delegateMarshallers.get(clazz);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void marshal(T sourceObject) throws IOException {
		Class clz = sourceObject.getClass();
		XmlSerializerSupport delegate = getDelegate(clz);
		if ( delegate == null ) {
			throw new UnsupportedOperationException("Unhandled "+clz); 
		} else {
			prepareChildMarshaller(delegate);
			delegate.marshal(sourceObject);
		}
	}
}
