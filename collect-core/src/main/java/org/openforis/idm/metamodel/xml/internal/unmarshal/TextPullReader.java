package org.openforis.idm.metamodel.xml.internal.unmarshal;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
abstract class TextPullReader extends IdmlPullReader {
	private boolean trimWhitespace;

	protected TextPullReader(String tagName) {
		this(tagName, null);
	}

	protected TextPullReader(String tagName, Integer maxCount) {
		super(tagName, maxCount);
		this.trimWhitespace = true;
	}
	
	protected boolean isTrim() {
		return trimWhitespace;
	}

	protected void setTrimWhitespace(boolean trim) {
		this.trimWhitespace = trim;
	}

	@Override
	protected void onStartTag() throws XmlPullParserException, IOException {
		XmlPullParser parser = getParser();
		String text = parser.nextText();
		processText(trimWhitespace ? text.trim() : text);
	}
	
	protected abstract void processText(String text);
}