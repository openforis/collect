package org.openforis.idm.metamodel.xml.internal.unmarshal;

import org.openforis.idm.metamodel.PlainTextApplicationOptions;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.ApplicationOptionsBinder;

/**
 * @author G. Miceli
 */
public class PlainTextApplicationOptionsBinder implements ApplicationOptionsBinder<PlainTextApplicationOptions> {

	@Override
	public PlainTextApplicationOptions unmarshal(Survey survey, String type, String body) {
		PlainTextApplicationOptions options = new PlainTextApplicationOptions();
		options.setType(type);
		options.setBody(body);
		return options;
	} 

	@Override
	public String marshal(PlainTextApplicationOptions options, String defaultLanguage) {
		return options.getBody();
	}

	@Override
	public boolean isSupported(String type) {
		return true;
	}
}
