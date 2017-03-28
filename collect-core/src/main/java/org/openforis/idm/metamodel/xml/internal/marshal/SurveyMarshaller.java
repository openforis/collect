package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.CREATED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CYCLE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.IDML3_NAMESPACE_URI;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LANGUAGE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LAST_ID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MODIFIED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PROJECT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PUBLISHED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SURVEY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.URI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.openforis.collect.utils.Dates;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.xmlpull.v1.XmlSerializer;


/**
 * Load a Survey object from IDML
 * 
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
public class SurveyMarshaller extends XmlSerializerSupport<Survey, Void>{

	private SurveyIdmlBinder binder;
	private SurveyMarshalParameters parameters;

	public SurveyMarshaller(SurveyIdmlBinder binder) {
		this(binder, true, false, false);
	}
	
	public SurveyMarshaller(SurveyIdmlBinder binder,
			boolean codeListsMarshalEnabled,
			boolean persistedCodeListsMarshalEnabled,
			boolean externalCodeListsMarshalEnabled) {
		this(binder, new SurveyMarshalParameters(
				codeListsMarshalEnabled,
				persistedCodeListsMarshalEnabled,
				externalCodeListsMarshalEnabled,
				null)
		);
	}
	
	public SurveyMarshaller(SurveyIdmlBinder binder, SurveyMarshalParameters parameters) {
		super(IDML3_NAMESPACE_URI, SURVEY);
		
		this.binder = binder;
		this.parameters = parameters;
		
		addChildMarshallers(
				new ProjectXS(),
				new UriXS(), 
				new CycleXS(),
				new DescriptionXS(),
				new LanguageXS(),
				new ApplicationOptionsXS(binder, parameters.getOutputSurveyDefaultLanguage()),
				new VersioningXS(), 
				new CodeListsXS(),
				new UnitsXS(),
				new SpatialReferenceSystemsXS(),
				new ReferenceDataSchemaXS(),
				new SchemaXS());
	}

	@Override
	public synchronized void marshal(Survey survey, OutputStream os,
			String enc) throws IOException {
		updateOutputDefaultLanguageParameter(survey);
		super.marshal(survey, os, enc);
	}

	@Override
	public synchronized void marshal(Survey survey, Writer wr, String enc)
			throws IOException {
		updateOutputDefaultLanguageParameter(survey);
		super.marshal(survey, wr, enc);
	}
	
	@Override
	protected void start(Survey survey) throws IOException {
		startDocument();
		setDefaultNamespace(IDML3_NAMESPACE_URI);
		setCustomNamespacePrefixes(survey);
		super.start(survey);
	}

	private void updateOutputDefaultLanguageParameter(Survey survey) {
		if (this.parameters.outputSurveyDefaultLanguage == null) {
			this.parameters.outputSurveyDefaultLanguage = survey.getDefaultLanguage();
		}
	}
	
	protected void setCustomNamespacePrefixes(Survey survey) throws IOException {
		List<String> uris = survey.getCustomNamespaces();
		for (String uri : uris) {
			XmlSerializer xs = getXmlSerializer();
			String prefix = survey.getCustomNamespacePrefix(uri);
			xs.setPrefix(prefix, uri);
		}
	}

	@Override
	protected void attributes(Survey survey) throws IOException {
		attribute(LAST_ID, survey.getLastId());
		attribute(PUBLISHED, survey.isPublished() ? true : null);
		attribute(CREATED, Dates.formatDateTime(survey.getCreationDate()));
		attribute(MODIFIED, Dates.formatDateTime(survey.getModifiedDate()));
		for (QName qname : survey.getAnnotationNames()) {
			String value = survey.getAnnotation(qname);
			attribute(qname.getNamespaceURI(), qname.getLocalPart(), value);
		}
	}
	
	@Override
	protected void end(Survey survey) throws IOException {
		super.end(survey);
		getXmlSerializer().endDocument();
	}

	private class ProjectXS extends LanguageSpecificTextXS<Survey> {
		public ProjectXS() {
			super(PROJECT);
		}

		@Override
		protected void marshalInstances(Survey survey) throws IOException {
			marshal(survey.getProjectNames(), parameters.outputSurveyDefaultLanguage);
		}
	}

	private class UriXS extends TextXS<Survey> {
		public UriXS() {
			super(URI);
		}

		@Override
		protected void marshalInstances(Survey survey) throws IOException {
			marshal(survey.getUri());
		}
	}

	private class CycleXS extends TextXS<Survey> {
		public CycleXS() {
			super(CYCLE);
		}

		@Override
		protected void marshalInstances(Survey survey) throws IOException {
			marshal(survey.getCycle());
		}
	}

	private class DescriptionXS extends LanguageSpecificTextXS<Survey> {
		public DescriptionXS() {
			super(DESCRIPTION);
		}

		@Override
		protected void marshalInstances(Survey survey) throws IOException {
			marshal(survey.getDescriptions(), parameters.outputSurveyDefaultLanguage);
		}
	}

	private class LanguageXS extends TextXS<Survey> {
		public LanguageXS() {
			super(LANGUAGE);
		}

		@Override
		protected void marshalInstances(Survey survey) throws IOException {
			Set<String> sortedLanguages = new LinkedHashSet<String>();
			sortedLanguages.add(((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage());
			sortedLanguages.addAll(survey.getLanguages());
			marshal(new ArrayList<String>(sortedLanguages));
		}
	}
	
	public SurveyIdmlBinder getBinder() {
		return binder;
	}

	public SurveyMarshalParameters getParameters() {
		return parameters;
	}
	
	/**
	 * 
	 * @deprecated Use the corresponding method in the parameters object
	 */
	@Deprecated
	public boolean isCodeListsMarshalEnabled() {
		return parameters.codeListsMarshalEnabled;
	}

	/**
	 * 
	 * @deprecated Use the corresponding method in the parameters object
	 */
	@Deprecated
	public boolean isPersistedCodeListsMarshalEnabled() {
		return parameters.persistedCodeListsMarshalEnabled;
	}

	/**
	 * 
	 * @deprecated Use the corresponding method in the parameters object
	 */
	@Deprecated
	public boolean isExternalCodeListsMarshalEnabled() {
		return parameters.externalCodeListsMarshalEnabled;
	}
	
	public static class SurveyMarshalParameters {
		private boolean codeListsMarshalEnabled;
		private boolean persistedCodeListsMarshalEnabled;
		private boolean externalCodeListsMarshalEnabled;
		private String outputSurveyDefaultLanguage;
		
		public SurveyMarshalParameters(
				boolean codeListsMarshalEnabled, 
				boolean persistedCodeListsMarshalEnabled,
				boolean externalCodeListsMarshalEnabled, 
				String outputSurveyDefaultLanguage) {
			super();
			this.codeListsMarshalEnabled = codeListsMarshalEnabled;
			this.persistedCodeListsMarshalEnabled = persistedCodeListsMarshalEnabled;
			this.externalCodeListsMarshalEnabled = externalCodeListsMarshalEnabled;
			this.outputSurveyDefaultLanguage = outputSurveyDefaultLanguage;
		}

		public boolean isCodeListsMarshalEnabled() {
			return codeListsMarshalEnabled;
		}
		
		public void setCodeListsMarshalEnabled(boolean codeListsMarshalEnabled) {
			this.codeListsMarshalEnabled = codeListsMarshalEnabled;
		}
		
		public boolean isExternalCodeListsMarshalEnabled() {
			return externalCodeListsMarshalEnabled;
		}
		
		public void setExternalCodeListsMarshalEnabled(boolean externalCodeListsMarshalEnabled) {
			this.externalCodeListsMarshalEnabled = externalCodeListsMarshalEnabled;
		}
		
		public String getOutputSurveyDefaultLanguage() {
			return outputSurveyDefaultLanguage;
		}
		
		public void setOutputSurveyDefaultLanguage(String outputSurveyDefaultLanguage) {
			this.outputSurveyDefaultLanguage = outputSurveyDefaultLanguage;
		}
		
		public boolean isPersistedCodeListsMarshalEnabled() {
			return persistedCodeListsMarshalEnabled;
		}
		
		public void setPersistedCodeListsMarshalEnabled(boolean persistedCodeListsMarshalEnabled) {
			this.persistedCodeListsMarshalEnabled = persistedCodeListsMarshalEnabled;
		}
		
	}
	
}
