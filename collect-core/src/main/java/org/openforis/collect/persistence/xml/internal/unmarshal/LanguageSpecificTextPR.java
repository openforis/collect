package org.openforis.collect.persistence.xml.internal.unmarshal;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.TYPE;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_NAMESPACE_URI;

import java.io.IOException;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.IdmlConstants;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.openforis.idm.metamodel.xml.internal.marshal.XmlPullReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
class LanguageSpecificTextPR extends XmlPullReader {
	private boolean requireType;
	
	public LanguageSpecificTextPR(String tagName, boolean requireType) {
		super(UI_NAMESPACE_URI, tagName);
		this.requireType = requireType;
	}
	
	public LanguageSpecificTextPR(String tagName) {
		this(tagName, false);
	}

	@Override
	protected void onStartTag() throws XmlParseException, XmlPullParserException, IOException {
		XmlPullParser parser = getParser();
		String lang = parser.getAttributeValue(IdmlConstants.XML_NS_URI, IdmlConstants.XML_LANG_ATTRIBUTE);
		String type = getAttribute(TYPE, requireType);
		String text = parser.nextText();
		processText(lang, type, text);
	}

	/** 
	 * Override this method to handle "type" attribute for other label types
	 * @param lang
	 * @param type
	 * @param text
	 * @throws XmlParseException 
	 */
	protected void processText(String lang, String type, String text) throws XmlParseException {
		LanguageSpecificText lst = new LanguageSpecificText(lang, text.trim());
		processText(lst);
	}
	
	protected void processText(LanguageSpecificText lst) {
		// no-op
	}
}
