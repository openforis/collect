package org.openforis.idm.metamodel.xml;

import static org.openforis.commons.io.OpenForisIOUtils.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.internal.marshal.SurveyMarshaller;
import org.openforis.idm.metamodel.xml.internal.marshal.SurveyMarshaller.SurveyMarshalParameters;
import org.openforis.idm.metamodel.xml.internal.unmarshal.SurveyUnmarshaller;

/**
 * Load a Survey object from IDML
 * 
 * @author G. Miceli
 */
public class SurveyIdmlBinder {
	
	private SurveyContext surveyContext;
	private List<ApplicationOptionsBinder<?>> optionsBinders;

	public SurveyIdmlBinder(SurveyContext surveyContext) {
		this.surveyContext = surveyContext;
		this.optionsBinders = new ArrayList<ApplicationOptionsBinder<?>>();
	}

	public void addApplicationOptionsBinder(ApplicationOptionsBinder<?> binder) {
		optionsBinders.add(binder);
	}

	/**
	 * 
	 * @param type
	 * @return the first binder which supports the specified type, or null
	 * if none found
	 */
	public ApplicationOptionsBinder<?> getApplicationOptionsBinder(String type) {
		for (ApplicationOptionsBinder<?> binder : optionsBinders) {
			if ( binder.isSupported(type) ) {
				return binder;
			}
		}
		return null;
	}
	
	public SurveyContext getSurveyContext() {
		return surveyContext;
	}
	
	public void marshal(Survey survey, OutputStream os) throws IOException {
		marshal(survey, os, true, false, false);
	}
	
	public void marshal(Survey survey, OutputStream os,
			boolean marshalCodeLists, 
			boolean marshalPersistedCodeLists,
			boolean marshalExternalCodeLists) throws IOException {
		marshal(survey, os, new SurveyMarshalParameters(
				marshalCodeLists, marshalPersistedCodeLists, marshalExternalCodeLists, survey.getDefaultLanguage()));
	}
	
	public void marshal(Survey survey, OutputStream os, SurveyMarshalParameters parameters) throws IOException {
		SurveyMarshaller ser = new SurveyMarshaller(this, parameters); 
		ser.marshal(survey, os, UTF_8);
	}

	public void marshal(Survey survey, Writer wr) throws IOException {
		marshal(survey, wr, true, false, false);
	}

	public void marshal(Survey survey, Writer wr,
			boolean marshalCodeLists, 
			boolean marshalExternalCodeLists,
			boolean marshalPersistedCodeLists) throws IOException {
		SurveyMarshaller ser = new SurveyMarshaller(this, new SurveyMarshalParameters(
				marshalCodeLists, marshalPersistedCodeLists, marshalExternalCodeLists, survey.getDefaultLanguage()));
		ser.marshal(survey, wr, UTF_8);
	}
		
	public Survey unmarshal(InputStream is) throws IdmlParseException {
		return unmarshal(is, true);
	}
	
	public Survey unmarshal(InputStream is, boolean includeCodeListItems) throws IdmlParseException {
		try {
			SurveyUnmarshaller unmarshaller = new SurveyUnmarshaller(this, includeCodeListItems);
			unmarshaller.parse(is, UTF_8);
			Survey survey = unmarshaller.getSurvey();
			onUnmarshallingComplete(survey);
			return survey;
		} catch (Exception e) {
			throw new IdmlParseException(e);
		}
	}

	public Survey unmarshal(Reader r) throws IdmlParseException {
		return unmarshal(r, true);
	}
	
	public Survey unmarshal(Reader r, boolean includeCodeListItems) throws IdmlParseException {
		try {
			SurveyUnmarshaller unmarshaller = new SurveyUnmarshaller(this, includeCodeListItems);
			unmarshaller.parse(r);
			Survey survey = unmarshaller.getSurvey();
			onUnmarshallingComplete(survey);
			return survey;
		} catch (Exception e) {
			throw new IdmlParseException(e);
		}
	}

	protected void onUnmarshallingComplete(Survey survey) {
		survey.init();
	}
	
}
