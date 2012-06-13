package org.openforis.collect.persistence.xml;

import org.xmlpull.v1.XmlSerializer;

/**
 * 
 * @author S. Ricci
 *
 */
public interface XmlSerializerFactory {

	public XmlSerializer getSerializer();
	
}
