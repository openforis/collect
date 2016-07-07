package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

import javax.xml.namespace.QName;

import org.openforis.collect.utils.Dates;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
public class SurveyUnmarshaller extends IdmlPullReader {

	protected Survey survey;
	private boolean includeCodeListItems;
	
	public SurveyUnmarshaller(SurveyIdmlBinder binder) {
		this(binder, true);
	}
	
	public SurveyUnmarshaller(SurveyIdmlBinder binder, boolean includeCodeListItems) {
		super(SURVEY);
		
		this.includeCodeListItems = includeCodeListItems;
		
		initChildren();

		setSurveyBinder(binder);
	}

	protected void initChildren() {
		addChildPullReaders(
			new ProjectPR(), 
			new UriPR(), 
			new CyclePR(),
			new DescriptionPR(),
			new LanguagePR(),
			new ApplicationOptionsPR(),
			new VersioningPR(), 
			createCodeListReader(),
			new UnitsPR(),
			new SpatialReferenceSystemsPR(),
			new ReferenceDataSchemaPR(),
			new SchemaPR());
	}

	protected XmlPullReader createCodeListReader() {
		return new CodeListsPR(includeCodeListItems);
	}

	@Override
	public Survey getSurvey() {
		return survey;
	}

	@Override
	public void onStartTag() throws XmlParseException {
		initSurvey();
		String lastId = getAttribute(LAST_ID, true);
		Boolean published = getBooleanAttribute(PUBLISHED, false);
		survey.setLastId(Integer.valueOf(lastId));
		survey.setPublished(published == null ? false : published);
		String creationDate = getAttribute(CREATED, false);
		if (creationDate != null) {
			survey.setCreationDate(Dates.parseDateTime(creationDate));
		}
		String modifiedDate = getAttribute(MODIFIED, false);
		if (modifiedDate != null) {
			survey.setModifiedDate(Dates.parseDateTime(modifiedDate));
		}
		readNamepaceDeclarations();
	}
	
	@Override
	protected void onEndTag() throws XmlParseException {
		super.onEndTag();
	}
	
	@Override
	protected void handleAnnotation(QName qName, String value) {
		survey.setAnnotation(qName, value);
	}

	protected void initSurvey() {
		SurveyIdmlBinder surveyBinder = getSurveyBinder();
		SurveyContext surveyContext = surveyBinder.getSurveyContext(); 
		this.survey = surveyContext.createSurvey();
	}

	protected void readNamepaceDeclarations() throws XmlParseException {
		XmlPullParser pp = getParser();
		try {
			int nsCount = pp.getNamespaceCount(1);
			for (int i = 0; i < nsCount; i++) {
				String prefix = pp.getNamespacePrefix(i);
				if ( prefix != null ) {
					String uri = pp.getNamespaceUri(i);
					survey.addCustomNamespace(uri, prefix);
				}
			}
		} catch (XmlPullParserException e) {
			throw new XmlParseException(pp, "Failed to read namespace declarations", e);
		}
	}

	// TAG READERS
	
	private class ProjectPR extends LanguageSpecificTextPR {
		public ProjectPR() {
			super(PROJECT);
		}
		
		@Override
		protected void processText(LanguageSpecificText lst) {
			survey.addProjectName(lst);
		}
	}
	
	private class UriPR extends TextPullReader {

		public UriPR() {
			super(URI, 1);
		}
		
		@Override
		protected void processText(String text) {
			survey.setUri(text);
		}
	}
	
	private class CyclePR extends TextPullReader {

		public CyclePR() {
			super(CYCLE, 1);
		}
		
		@Override
		protected void processText(String text) {
			survey.setCycle(text);
		}
	}
	
	private class DescriptionPR extends LanguageSpecificTextPR {

		public DescriptionPR() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void processText(LanguageSpecificText lst) {
			survey.addDescription(lst);
		}
	}
	
	private class LanguagePR extends TextPullReader {

		public LanguagePR() {
			super(LANGUAGE);
		}

		@Override
		protected void processText(String text) {
			survey.addLanguage(text);
		}
		
	}
}
