package org.openforis.collect.persistence.xml;

import org.xmlpull.v1.XmlSerializer;

/**
 * 
 * @author S. Ricci
 *
 */
public class XmlPullSerializerFactory implements XmlSerializerFactory {

	@Override
	public XmlSerializer getSerializer() {
		return new XmlPullSerializer();
	}
	
}
