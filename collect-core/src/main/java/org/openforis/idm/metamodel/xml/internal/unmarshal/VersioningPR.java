package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.DATE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LABEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.NAME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.VERSION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.VERSIONING;

import org.openforis.collect.utils.Dates;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.XmlParseException;

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
				version.setDate(Dates.parseDate(text));
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