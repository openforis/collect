package org.openforis.collect.persistence.xml;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlSerializer;

/**
 * 
 * @author S. Ricci
 *
 */
public final class XmlPullSerializerFactory {

	public static XmlPullSerializerFactory createInstance() {
		return new XmlPullSerializerFactory();
	}
	
	public XmlSerializer createSerializer() {
		return new KXmlSerializer();
	}
	
	
	
}
