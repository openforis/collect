package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.APPLICATION_OPTIONS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.OPTIONS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TYPE;

import java.io.IOException;

import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.ApplicationOptionsBinder;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
class ApplicationOptionsPR extends IdmlPullReader {

	public ApplicationOptionsPR() {
		super(APPLICATION_OPTIONS, 1);
		addChildPullReaders(new OptionsPR());
	}

	private class OptionsPR extends IdmlPullReader {

		OptionsPR() {
			super(OPTIONS);
		}

		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			String type = getAttribute(TYPE, true);
//			String ns = getParser().getNamespaceUri(getParser().getDepth()-1);
			String body = readElement(false);
			SurveyIdmlBinder binder = getSurveyBinder();
			ApplicationOptionsBinder<?> optionsBinder = binder.getApplicationOptionsBinder(type);
			if ( optionsBinder != null ) {
				Survey survey = getSurvey();
				ApplicationOptions options = optionsBinder.unmarshal(survey, type, body);
				survey.addApplicationOptions(options);
			}
		}
	}

}