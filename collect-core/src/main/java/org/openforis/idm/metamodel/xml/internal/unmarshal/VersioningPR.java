package org.openforis.idm.metamodel.xml.internal.unmarshal;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.XmlParseException;
import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
class VersioningPR extends IdmlPullReader {
	
	public VersioningPR() {
		super(VERSIONING, 1);
		addChildPullReaders(new VersionPR());
	}

	private class VersionPR extends IdmlPullReader {

		private ModelVersion version;
		
		public VersionPR() {
			super(VERSION);
			addChildPullReaders(new LabelPR(), new DescriptionPR(), new DatePR());
		}
		
		@Override
		protected void onStartTag() throws XmlParseException {
			int id = getIntegerAttribute(ID, true);
			String name = getAttribute(NAME, false);
			Survey survey = getSurvey();
			version = survey.createModelVersion(id);
			version.setName(name);
		}

		private class LabelPR extends LanguageSpecificTextPR {
			public LabelPR() {
				super(LABEL);
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				version.addLabel(lst);
			}
		}

		private class DescriptionPR extends LanguageSpecificTextPR {
			public DescriptionPR() {
				super(DESCRIPTION);
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				version.addDescription(lst);
			}
		}

		private class DatePR extends TextPullReader {
			public DatePR() {
				super(DATE, 1);
			}
			
			@Override
			protected void processText(String text) {
				version.setDate(text);
			}
		}
	
		@Override
		protected void onEndTag() throws XmlParseException {
			Survey survey = version.getSurvey();
			survey.addVersion(version);
			this.version = null;
		}
	}
}