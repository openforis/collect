/**
 * 
 */
package org.openforis.idm.metamodel.xml.internal.unmarshal;

import java.io.IOException;

import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author S. Ricci
 *
 */
public class SkipElementPR extends IdmlPullReader {

	protected SkipElementPR(String tagName) {
		super(tagName);
	}
	
	@Override
	protected void onStartTag()
			throws XmlParseException, XmlPullParserException, IOException {
		skipElement();
	}
	
}
