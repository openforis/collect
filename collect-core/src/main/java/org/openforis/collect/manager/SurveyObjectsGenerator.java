package org.openforis.collect.manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
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
		addPredefinedSRSs(survey);
	}
	
	protected void addPredefinedUnits(Survey survey) {
		Set<String> labelLanguages = new HashSet<String>(survey.getLanguages());
		labelLanguages.add("en");
		
		//ANGLE
		addUnit(survey, "deg", Dimension.ANGLE, null,
				Arrays.asList("degrees"), 
				Arrays.asList("deg"),
				labelLanguages);
		//LENGTH
		addUnit(survey, "m", Dimension.LENGTH, 1.0,
				Arrays.asList("meters"), 
				Arrays.asList("m"),
				labelLanguages);
		
		addUnit(survey, "dm", Dimension.LENGTH, 0.1,
				Arrays.asList("decimeters"), 
				Arrays.asList("dm"),
				labelLanguages);
		
		addUnit(survey, "cm", Dimension.LENGTH, 0.01,
				Arrays.asList("centimeters"), 
				Arrays.asList("cm"),
				labelLanguages);
		
		addUnit(survey, "mm", Dimension.LENGTH, 0.001,
				Arrays.asList("millimeters"), 
				Arrays.asList("mm"),
				labelLanguages);
		
		addUnit(survey, "km", Dimension.LENGTH, 1000.0,
				Arrays.asList("kilometers"), 
				Arrays.asList("km"),
				labelLanguages);
		//AREA
		addUnit(survey, "ac", Dimension.AREA, 2.47105381,
				Arrays.asList("acres"),
				Arrays.asList("ac"),
				labelLanguages);
		
		addUnit(survey, "ha", Dimension.AREA, 1.0,
				Arrays.asList("hectares"), 
				Arrays.asList("ha"),
				labelLanguages);
		//RATIO
		addUnit(survey, "percent", Dimension.RATIO, 0.01,
				Arrays.asList("percent"), 
				Arrays.asList("%"),
				labelLanguages);
		
		//VOLUME
		addUnit(survey, "cm3", Dimension.VOLUME, 0.001,
				Arrays.asList("Cubic centimeters"), 
				Arrays.asList("cm3"),
				labelLanguages);
		
		addUnit(survey, "l", Dimension.VOLUME, 1.0,
				Arrays.asList("Litres"), 
				Arrays.asList("l"),
				labelLanguages);
		
		addUnit(survey, "m3", Dimension.VOLUME, 1000.0,
				Arrays.asList("Cubic metres"), 
				Arrays.asList("m3"),
				labelLanguages);
	}
	
	protected void addPredefinedSRSs(Survey survey) {
		CoordinateOperations coordinateOperations = survey.getContext().getCoordinateOperations();
		if ( coordinateOperations != null ) {
			List<SpatialReferenceSystem> predefinedSRSs = Arrays.asList(new SpatialReferenceSystem(SpatialReferenceSystem.LAT_LON_SRS));
			for (SpatialReferenceSystem srs : predefinedSRSs) {
				survey.addSpatialReferenceSystem(srs);
			}
		}
		return;
	}
	
	private void addUnit(Survey survey, String name, Dimension dimension,
			Double conversionFactor,
			List<String> labels,
			List<String> abbreviations,
			Set<String> labelLanguages) {
		Unit unit = survey.createUnit();
		unit.setName(name);
		unit.setDimension(dimension.name().toLowerCase(Locale.ENGLISH));
		unit.setConversionFactor(conversionFactor);
		for (String label : labels) {
			for (String lang : labelLanguages) {
				unit.setLabel(lang, label);
			}
		}
		for (String abbr : abbreviations) {
			for (String lang : labelLanguages) {
				unit.setAbbreviation(lang, abbr);
			}
		}
		survey.addUnit(unit);
	}
	
}
