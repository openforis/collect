/**
 * 
 */
package org.openforis.collect.persistence.xml;

import java.io.StringReader;

import org.openforis.collect.model.ui.UIOptions;
import org.openforis.idm.metamodel.xml.ApplicationOptionsBinder;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author S. Ricci
 *
 */
public class UIOptionsBinder implements
		ApplicationOptionsBinder<UIOptions> {

	@Override
	public UIOptions unmarshal(String body) {
		XmlPullParser parser = null;
		try {
			UIOptionsPullReader uiOptionsReader = new UIOptionsPullReader(this);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			parser = factory.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			StringReader reader = new StringReader(body);
			parser.setInput(reader);
			uiOptionsReader.parse(parser);
			
			return uiOptionsReader.getUIOptions();
		} catch (XmlPullParserException e) {
			throw new XmlParseException(parser, e.getMessage(), e);
		}
	}

	@Override
	public String marshal(UIOptions options) {
		return null;
	}

	@Override
	public boolean isSupported(String optionsType) {
		if ( UIOptions.UI_TYPE.equals(optionsType) ) {
			return true;
		} else {
			return false;
		}
	}
	
	

}
