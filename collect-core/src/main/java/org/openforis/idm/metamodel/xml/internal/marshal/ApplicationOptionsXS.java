package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.APPLICATION_OPTIONS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.OPTIONS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TYPE;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.ApplicationOptionsBinder;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;

/**
 * 
 * @author G. Miceli
 *
 */
class ApplicationOptionsXS extends XmlSerializerSupport<ApplicationOptions, Survey> {

	private SurveyIdmlBinder binder;
	private String defaultLanguage;

	ApplicationOptionsXS(SurveyIdmlBinder binder, String defaultLanguage) {
		super(OPTIONS);
		setListWrapperTag(APPLICATION_OPTIONS);
		this.binder = binder;
		this.defaultLanguage = defaultLanguage;
	}
	
	@Override
	protected void marshalInstances(Survey survey) throws IOException {
		List<ApplicationOptions> options = survey.getApplicationOptions();
		marshal(options);
	}
	
	@Override
	protected void attributes(ApplicationOptions options) throws IOException {
		attribute(TYPE, options.getType());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void body(ApplicationOptions options) throws IOException {
		String namespaceUri = options.getType();
		setDefaultNamespace(namespaceUri);
		ApplicationOptionsBinder optionsBinder = binder.getApplicationOptionsBinder(namespaceUri);
		String xml = optionsBinder.marshal(options, defaultLanguage);
		writeXml(xml);
	}
}
