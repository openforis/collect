package org.openforis.collect.persistence.xml.internal.unmarshal;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.TYPE;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_NAMESPACE_URI;
import static org.openforis.idm.metamodel.xml.IdmlConstants.XML_LANG_ATTRIBUTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.XML_NAMESPACE_URI;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
class LanguageSpecificTextPR extends UIElementPullReader {
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
		String lang = parser.getAttributeValue(XML_NAMESPACE_URI, XML_LANG_ATTRIBUTE);
		if ( StringUtils.isBlank(lang) ) {
			lang = getSurvey().getDefaultLanguage();
		}
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
