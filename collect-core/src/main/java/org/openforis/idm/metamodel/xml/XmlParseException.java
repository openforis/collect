package org.openforis.idm.metamodel.xml;

import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author G. Miceli
 */
public class XmlParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public XmlParseException(XmlPullParser parser, String msg) {
		this(parser, msg, null);
	}
	
	public XmlParseException(XmlPullParser parser, String message, Throwable cause) {
		super("error: " + StringUtils.trimToEmpty(message)+ " position: " + (parser == null ? "": parser.getPositionDescription()), cause);
	}
	
}
