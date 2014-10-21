package org.openforis.idm.metamodel.xml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author G. Miceli
 */
public class XmlParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public XmlParseException(XmlPullParser parser, String message, Throwable cause) {
		super(message+" "+parser.getPositionDescription(), cause);
	}
	
	public XmlParseException(XmlPullParser parser, String msg) {
		super(msg+" "+parser == null ? null : parser.getPositionDescription());
	}
}
