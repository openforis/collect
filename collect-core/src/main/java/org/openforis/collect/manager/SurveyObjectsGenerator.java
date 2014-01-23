package org.openforis.collect.manager;

import java.util.Arrays;
import java.util.List;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.Unit.Dimension;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyObjectsGenerator {
	
	public void addPredefinedObjects(Survey survey) {
		addPredefinedUnits(survey);
		addPredefinedSRS(survey);
	}

	protected void addPredefinedUnits(Survey survey) {
		//ANGLE
		addUnit(survey, "deg", Dimension.ANGLE, null,
				Arrays.asList(new LanguageSpecificText("en", "Degrees")), 
				Arrays.asList(new LanguageSpecificText("en", "deg")));
		//LENGTH
		addUnit(survey, "m", Dimension.LENGTH, 1.0,
				Arrays.asList(new LanguageSpecificText("en", "metres")), 
				Arrays.asList(new LanguageSpecificText("en", "m")));
		
		addUnit(survey, "dm", Dimension.LENGTH, 0.1,
				Arrays.asList(new LanguageSpecificText("en", "decimeters")), 
				Arrays.asList(new LanguageSpecificText("en", "dm")));
		
		addUnit(survey, "cm", Dimension.LENGTH, 0.01,
				Arrays.asList(new LanguageSpecificText("en", "centimeters")), 
				Arrays.asList(new LanguageSpecificText("en", "cm")));
		
		addUnit(survey, "mm", Dimension.LENGTH, 0.001,
				Arrays.asList(new LanguageSpecificText("en", "millimeters")), 
				Arrays.asList(new LanguageSpecificText("en", "mm")));
		
		addUnit(survey, "km", Dimension.LENGTH, 1000.0,
				Arrays.asList(new LanguageSpecificText("en", "kilometers")), 
				Arrays.asList(new LanguageSpecificText("en", "km")));
		//AREA
		addUnit(survey, "ac", Dimension.AREA, 2.47105381,
				Arrays.asList(new LanguageSpecificText("en", "acres")), 
				Arrays.asList(new LanguageSpecificText("en", "ac")));
		
		addUnit(survey, "ha", Dimension.AREA, 1.0,
				Arrays.asList(new LanguageSpecificText("en", "hectares")), 
				Arrays.asList(new LanguageSpecificText("en", "ha")));
		//RATIO
		addUnit(survey, "percent", Dimension.RATIO, 0.01,
				Arrays.asList(new LanguageSpecificText("en", "percent")), 
				Arrays.asList(new LanguageSpecificText("en", "%")));
	}
	
	protected void addPredefinedSRS(Survey survey) {
		//TODO
	}
	
	private void addUnit(Survey survey, String name, Dimension dimension,
			Double conversionFactor,
			List<LanguageSpecificText> labels,
			List<LanguageSpecificText> abbreviations) {
		Unit unit = survey.createUnit();
		unit.setName(name);
		unit.setDimension(dimension.name());
		unit.setConversionFactor(conversionFactor);
		for (LanguageSpecificText label : labels) {
			unit.addLabel(label);
		}
		for (LanguageSpecificText abbreviation : abbreviations) {
			unit.addAbbreviation(abbreviation);
		}
		survey.addUnit(unit);
	}
	
}
