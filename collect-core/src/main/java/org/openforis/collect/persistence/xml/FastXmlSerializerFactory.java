package org.openforis.collect.persistence.xml;

import org.xmlpull.v1.XmlSerializer;

/**
 * 
 * @author S. Ricci
 *
 */
public class FastXmlSerializerFactory implements XmlSerializerFactory {

	@Override
	public XmlSerializer newSerializer() {
		return new FastXmlSerializer();
	}
	
}
