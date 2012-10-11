/**
 * 
 */
package org.openforis.collect.persistence.xml;

import java.io.StringReader;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.collect.persistence.DataInconsistencyException;
import org.openforis.idm.metamodel.xml.ApplicationOptionsBinder;
import org.xmlpull.v1.XmlPullParser;
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
			UIOptionsPullReader uiOptionsReader = new UIOptionsPullReader();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			parser = factory.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			StringReader reader = new StringReader(body);
			parser.setInput(reader);
			uiOptionsReader.parse(parser);
			return uiOptionsReader.getUIOptions();
		} catch (Exception e) {
			throw new DataInconsistencyException(e.getMessage(), e);
		}
	}

	@Override
	public String marshal(UIOptions options) {
		//TODO
		return null;
	}

	@Override
	public boolean isSupported(String optionsType) {
		if ( UIOptionsConstants.UI_TYPE.equals(optionsType) ) {
			return true;
		} else {
			return false;
		}
	}
	
	

}
