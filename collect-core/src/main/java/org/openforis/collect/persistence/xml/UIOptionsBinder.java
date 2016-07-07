/**
 * 
 */
package org.openforis.collect.persistence.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.DataInconsistencyException;
import org.openforis.collect.persistence.xml.internal.marshal.UIOptionsSerializer;
import org.openforis.collect.persistence.xml.internal.unmarshal.UITabSetPR;
import org.openforis.idm.metamodel.Survey;
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
	public UIOptions unmarshal(Survey survey, String type, String body) {
		XmlPullParser parser = null;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			parser = factory.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			Reader reader = new StringReader(body);
			parser.setInput(reader);
			UIOptions uiOptions = new UIOptions((CollectSurvey) survey);
			UITabSet tabSet = unmarshalTabSet(parser, uiOptions);
			while ( tabSet != null ) {
				uiOptions.addTabSet(tabSet);
				tabSet = unmarshalTabSet(parser, uiOptions);
			}
			return uiOptions;
		} catch (Exception e) {
			throw new DataInconsistencyException(e.getMessage(), e);
		}
	}

	private UITabSet unmarshalTabSet(XmlPullParser parser, UIOptions uiOptions) throws IOException, XmlPullParserException, XmlParseException {
		try {
			UITabSetPR tabSetPR = new UITabSetPR(this, uiOptions);
			tabSetPR.parse(parser);
			UITabSet tabSet = tabSetPR.getTabSet();
			return tabSet;
		} catch ( XmlParseException e) {
			if ( parser != null && parser.getEventType() == XmlPullParser.END_DOCUMENT ) {
				return null;
			} else {
				throw e;
			}
		}
	}

	@Override
	public String marshal(UIOptions options, String defaultLanguage) {
		try {
			UIOptionsSerializer serializer = new UIOptionsSerializer();
			Writer writer = new StringWriter();
			serializer.write(options, writer, defaultLanguage);
			String result = writer.toString();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Error serializing UIOptions", e);
		}
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
