package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author G. Miceli
 *
 */
class UnitsXS extends XmlSerializerSupport<Unit, Survey> {

	UnitsXS() {
		super(UNIT);
		setListWrapperTag(UNITS);
		addChildMarshallers(new LabelXS(), new AbbreviationXS());
	}
	
	@Override
	protected void marshalInstances(Survey survey) throws IOException {
		List<Unit> units = survey.getUnits();
		marshal(units);
	}
	
	@Override
	protected void attributes(Unit unit) throws IOException {
		attribute(ID, unit.getId());
		attribute(NAME, unit.getName());
		attribute(DIMENSION, unit.getDimension());
		attribute(CONVERSION_FACTOR, unit.getConversionFactor());
	}
	
	private class LabelXS extends LanguageSpecificTextXS<Unit> {

		public LabelXS() {
			super(LABEL);
		}
		
		@Override
		protected void marshalInstances(Unit unit) throws IOException {
			marshal(unit.getLabels());
		}
	}
	
	private class AbbreviationXS extends LanguageSpecificTextXS<Unit> {

		public AbbreviationXS() {
			super(ABBREVIATION);
		}
		
		@Override
		protected void marshalInstances(Unit unit) throws IOException {
			marshal(unit.getAbbreviations());
		}
	}
}
