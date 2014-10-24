package org.openforis.idm.metamodel.xml.internal.unmarshal;

import java.io.IOException;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;
import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
public class UnitsPR extends IdmlPullReader {

	public UnitsPR() {
		super(UNITS, 1);
		addChildPullReaders(new UnitPR());
	}
	
	private class UnitPR extends IdmlPullReader {

		private Unit unit;
		
		public UnitPR() {
			super(UNIT);
			addChildPullReaders(new LabelPR(), new AbbreviationPR());
		}

		@Override
		protected void onStartTag() throws XmlParseException, XmlPullParserException, IOException {
			int id = getIntegerAttribute(ID, true);
			String name = getAttribute(NAME, true);
			String dimension = getAttribute(DIMENSION, true);
			Double conversionFactor = getDoubleAttribute(CONVERSION_FACTOR, false);
			Survey survey = getSurvey();
			this.unit = survey.createUnit(id);
			unit.setName(name);
			unit.setDimension(dimension);
			unit.setConversionFactor(conversionFactor);
		}
		
		private class LabelPR extends LanguageSpecificTextPR {
			public LabelPR() {
				super(LABEL);
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				unit.addLabel(lst);
			}
		}

		private class AbbreviationPR extends LanguageSpecificTextPR {
			public AbbreviationPR() {
				super(ABBREVIATION);
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				unit.addAbbreviation(lst);
			}
		}
		
		@Override
		protected void onEndTag() throws XmlParseException {
			Survey survey = getSurvey();
			survey.addUnit(unit);
		}
	}
}