package org.openforis.collect.persistence.xml;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.ApplicationOptionsBinder;

import com.google.gson.Gson;

public class CeoApplicationOptionsBinder implements ApplicationOptionsBinder<CeoApplicationOptions> {

	private Gson gson = new Gson();

	@Override
	public boolean isSupported(String type) {
		return CeoApplicationOptions.TYPE.equals(type);
	}
	
	@Override
	public CeoApplicationOptions unmarshal(Survey survey, String type, String body) {
		CeoApplicationOptions result = gson.fromJson(body, CeoApplicationOptions.class);
		return result;
	}

	@Override
	public String marshal(CeoApplicationOptions options, String defaultLanguage) {
		return gson.toJson(options);
	}

	

}
