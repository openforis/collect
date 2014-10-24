package org.openforis.idm.metamodel.xml.internal.marshal;

import java.io.IOException;

/**
 * 
 * @author G. Miceli
 *
 */
abstract class TextXS<P> extends XmlSerializerSupport<String, P>{

	public TextXS(String tag) {
		super(tag);
	}

	@Override
	protected void body(String text) throws IOException {
		text(text);
	}
	
	
}
