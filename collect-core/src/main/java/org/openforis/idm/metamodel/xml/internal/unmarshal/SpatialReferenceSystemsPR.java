package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LABEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SPATIAL_REFERENCE_SYSTEM;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SPATIAL_REFERENCE_SYSTEMS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SRID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.WKT;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.XmlParseException;

/**
 * @author G. Miceli
 */
class SpatialReferenceSystemsPR extends IdmlPullReader {
	
	public SpatialReferenceSystemsPR() {
		super(SPATIAL_REFERENCE_SYSTEMS, 1);
		addChildPullReaders(new SrsPR());
	}

	private class SrsPR extends IdmlPullReader {

		private SpatialReferenceSystem srs;
		
		public SrsPR() {
			super(SPATIAL_REFERENCE_SYSTEM);
			addChildPullReaders(new LabelPR(), new DescriptionPR(), new WktPR());
		}
		
		@Override
		protected void onStartTag() throws XmlParseException {
			String id = getAttribute(SRID, true);
			this.srs = new SpatialReferenceSystem();
			srs.setId(id);
		}

		private class LabelPR extends LanguageSpecificTextPR {
			public LabelPR() {
				super(LABEL);
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				srs.addLabel(lst);
			}
		}

		private class DescriptionPR extends LanguageSpecificTextPR {
			public DescriptionPR() {
				super(DESCRIPTION);
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				srs.addDescription(lst);
			}
		}

		private class WktPR extends TextPullReader {

			public WktPR() {
				super(WKT, 1);
				setTrimWhitespace(true);
			}

			@Override
			protected void processText(String text) {
				srs.setWellKnownText(text);
			}
		}
	
		@Override
		protected void onEndTag() throws XmlParseException {
			Survey survey = getSurvey();
			survey.addSpatialReferenceSystem(srs);
			this.srs = null;
		}
	}
}