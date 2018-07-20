package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.IDML3_NAMESPACE_URI;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
abstract class IdmlPullReader extends XmlPullReader {

	private SurveyIdmlBinder binder;

	IdmlPullReader(String tagName) {
		super(IDML3_NAMESPACE_URI, tagName);
	}
	
	IdmlPullReader(String tagName, Integer maxCount) {
		super(IDML3_NAMESPACE_URI, tagName, maxCount);
	}
	
	SurveyIdmlBinder getSurveyBinder() {
		return binder;
	}
	
	void setSurveyBinder(SurveyIdmlBinder binder) {
		this.binder = binder;
		if ( binder != null ) {
			List<XmlPullReader> childPullReaders = getChildPullReaders();
			if ( childPullReaders != null ) {
				for (XmlPullReader reader : childPullReaders) {
					IdmlPullReader idmlPullReader = (IdmlPullReader) reader;
					if ( idmlPullReader.binder == null ) {
						idmlPullReader.setSurveyBinder(binder);
					}
				}
			}
		}
	}

	public Survey getSurvey() {
		XmlPullReader parent = getParentReader();
		while ( parent != null ) {
			if ( parent instanceof SurveyUnmarshaller ) {
				return ((SurveyUnmarshaller) parent).getSurvey();
			}
			parent = parent.getParentReader();
		}
		return null;
	}


	protected void handleAnnotation(QName qName, String value) {
		// no-op
	}

	@Override
	protected final void parseTag() throws XmlPullParserException, IOException,
			XmlParseException {
		onStartTag();
		
		parseAnnotations();
		
		parseTagBody();
		
		onEndTag();
	}

	private void parseAnnotations() {
		XmlPullParser parser = getParser();
		for (int i=0; i < parser.getAttributeCount(); i++) {
			String ns = parser.getAttributeNamespace(i);
			String prefix = parser.getAttributePrefix(i);
			if ( prefix != null && !IDML3_NAMESPACE_URI.equals(ns) ) {
				String name = parser.getAttributeName(i);
				String value = parser.getAttributeValue(i);
				handleAnnotation(new QName(ns, name, prefix), value);
			}
		}
	}
}