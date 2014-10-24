package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.CYCLE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.IDML3_NAMESPACE_URI;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LANGUAGE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LAST_ID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PROJECT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PUBLISHED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SURVEY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.URI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

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

	private boolean codeListsMarshalEnabled;
	private boolean persistedCodeListsMarshalEnabled;
	private boolean externalCodeListsMarshalEnabled;
	private SurveyIdmlBinder binder;

	public SurveyMarshaller(SurveyIdmlBinder binder) {
		this(binder, true, false, false);
	}
	
	public SurveyMarshaller(SurveyIdmlBinder binder,
			boolean codeListsMarshalEnabled,
			boolean persistedCodeListsMarshalEnabled,
			boolean externalCodeListsMarshalEnabled) {
		super(IDML3_NAMESPACE_URI, SURVEY);
		
		this.binder = binder;
		this.codeListsMarshalEnabled = codeListsMarshalEnabled;
		this.persistedCodeListsMarshalEnabled = persistedCodeListsMarshalEnabled;
		this.externalCodeListsMarshalEnabled = externalCodeListsMarshalEnabled;
		
		addChildMarshallers(
				new ProjectXS(),
				new UriXS(), 
				new CycleXS(),
				new DescriptionXS(),
				new LanguageXS(),
				new ApplicationOptionsXS(binder),
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
		super.marshal(survey, os, enc);
	}
	
	@Override
	public synchronized void marshal(Survey survey, Writer wr, String enc)
			throws IOException {
		super.marshal(survey, wr, enc);
	}
	
	@Override
	protected void start(Survey survey) throws IOException {
		startDocument();
		setDefaultNamespace(IDML3_NAMESPACE_URI);
		setCustomNamespacePrefixes(survey);
		super.start(survey);
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
			marshal(survey.getProjectNames(), survey.getDefaultLanguage());
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
			marshal(survey.getDescriptions(), survey.getDefaultLanguage());
		}
	}

	private class LanguageXS extends TextXS<Survey> {
		public LanguageXS() {
			super(LANGUAGE);
		}

		@Override
		protected void marshalInstances(Survey survey) throws IOException {
			marshal(survey.getLanguages());
		}
	}
	
	public SurveyIdmlBinder getBinder() {
		return binder;
	}

	public boolean isCodeListsMarshalEnabled() {
		return codeListsMarshalEnabled;
	}

	public boolean isPersistedCodeListsMarshalEnabled() {
		return persistedCodeListsMarshalEnabled;
	}

	public boolean isExternalCodeListsMarshalEnabled() {
		return externalCodeListsMarshalEnabled;
	}
	
}
