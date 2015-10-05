/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jooq.TableField;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NameValueEntry;
import org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * @author D. Wiell
 * 
 */
public abstract class DatabaseLookupProvider implements LookupProvider {

	private static final String SURVEY_ID_FIELD = "survey_id";

	@Override
	public Object lookup(Survey survey, String name, String attribute, Object... columns) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		addSurveyFilter(filters, survey);
		filters.addAll(Arrays.asList(NameValueEntry.fromKeyValuePairs(columns)));
		Object object = loadValue(name, attribute, filters.toArray(new NameValueEntry[0]));
		return object;
	}
	
	protected abstract Object loadValue(String name, String attribute, NameValueEntry[] filters);
	
	@Override
	public Coordinate lookupSamplingPointCoordinate(Survey survey, String... keys) {
		String valueColumnName = OfcSamplingDesign.OFC_SAMPLING_DESIGN.LOCATION.getName();
		Object value = lookupSamplingPointColumnValue(survey, valueColumnName, keys);
		Coordinate coordinate = Coordinate.parseCoordinate(value);
		return coordinate;
	}

	@Override
	public Object lookupSamplingPointData(Survey survey, String attribute, String... keys) {
		String columnName = convertToSamplingPointColumnName(survey, attribute);
		return lookupSamplingPointColumnValue(survey, columnName, keys);
	}

	private Object lookupSamplingPointColumnValue(Survey survey, String valueColumnName, String... keys) {
		int maxKeys = SamplingDesignDao.LEVEL_CODE_FIELDS.length;
		if ( keys == null || keys.length == 0 || keys.length > maxKeys ) {
			throw new IllegalArgumentException(String.format("Invalid number of keys. It should be between 1 and %d", maxKeys));
		}
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		
		addSurveyFilter(filters, survey);
		
		String[] paddedKeys = Arrays.copyOf(keys, maxKeys);
		for (int i = 0; i < paddedKeys.length; i++) {
			String key = paddedKeys[i];
			TableField<?, ?> keyField = SamplingDesignDao.LEVEL_CODE_FIELDS[i];
			NameValueEntry filter = new NameValueEntry(keyField.getName(), key);
			filters.add(filter);
		}
		Object value = loadValue(OfcSamplingDesign.OFC_SAMPLING_DESIGN.getName(), valueColumnName, filters.toArray(new NameValueEntry[filters.size()]));
		return value;
	}

	private String convertToSamplingPointColumnName(Survey survey, String attribute) {
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		SamplingPointDefinition samplingPoint = referenceDataSchema == null ? null: referenceDataSchema.getSamplingPointDefinition();
		if ( samplingPoint != null ) {
			List<ReferenceDataDefinition.Attribute> infoAttributes = samplingPoint.getAttributes(false);
			for (int i = 0; i < infoAttributes.size(); i++) {
				ReferenceDataDefinition.Attribute infoAttr = infoAttributes.get(i);
				if ( infoAttr.getName().equals(attribute) ) {
					return SamplingDesignDao.INFO_FIELDS[i].getName();
				}
			}
		}
		return attribute;
	}

	private void addSurveyFilter(List<NameValueEntry> filters, Survey survey) {
		if ( survey instanceof CollectSurvey ) {
			Integer surveyId = survey.getId();
			if ( surveyId != null ) {
				filters.add(new NameValueEntry(SURVEY_ID_FIELD, surveyId.toString()));
			}
		}
	}

}
