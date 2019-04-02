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
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * @author D. Wiell
 * 
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class DatabaseLookupProvider implements LookupProvider {

	private static final String SURVEY_ID_FIELD = "survey_id";
	
	@Autowired
	private DynamicTableDao dynamicTableDao;
	@Autowired
	private SamplingDesignDao samplingDesignDao; 

	@Override
	public Object lookup(Survey survey, String name, String attribute, Object... keyValuePairs) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		addSurveyFilter(filters, survey);
		filters.addAll(Arrays.asList(NameValueEntry.fromKeyValuePairs(keyValuePairs)));
		return loadValue(survey.getId(), name, attribute, filters.toArray(new NameValueEntry[filters.size()]));
	}
	
	@Override
	public Coordinate lookupSamplingPointCoordinate(Survey survey, String... keys) {
		SamplingDesignItem samplingDesignItem = samplingDesignDao.loadItem(survey.getId(), keys);
		return samplingDesignItem == null ? null : samplingDesignItem.getCoordinate();
	}

	@Override
	public Object lookupSamplingPointData(Survey survey, String attribute, String... keys) {
		int attributeIndex = getInfoAttributeIndex(survey, attribute);
		if (attributeIndex >= 0) {
			SamplingDesignItem samplingDesignItem = samplingDesignDao.loadItem(survey.getId(), keys);
			return samplingDesignItem == null ? null : samplingDesignItem.getInfoAttribute(attributeIndex);
		} else {
			return null;
		}
	}

	private Object loadValue(int surveyId, String tableName, String attribute, NameValueEntry[] filters) {
		if (OfcSamplingDesign.OFC_SAMPLING_DESIGN.getName().equals(tableName)) {
			String[] parentKeys = toParentKeys(filters);
			return samplingDesignDao.loadItem(surveyId, parentKeys);
		} else {
			return dynamicTableDao.loadValue(tableName, attribute, filters);
		}
	}

	private int getInfoAttributeIndex(Survey survey, String attribute) {
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		SamplingPointDefinition samplingPoint = referenceDataSchema == null ? null: referenceDataSchema.getSamplingPointDefinition();
		if ( samplingPoint != null ) {
			List<ReferenceDataDefinition.Attribute> infoAttributes = samplingPoint.getAttributes(false);
			for (int i = 0; i < infoAttributes.size(); i++) {
				ReferenceDataDefinition.Attribute infoAttr = infoAttributes.get(i);
				if ( infoAttr.getName().equals(attribute) ) {
					return i;
				}
			}
		}
		return -1;
	}

	private String[] toParentKeys(NameValueEntry[] filters) {
		TableField<?,?>[] levelCodeFields = SamplingDesignDao.LEVEL_CODE_FIELDS;
		String[] parentKeys = new String[levelCodeFields.length];
		for (int i = 0; i < levelCodeFields.length; i++) {
			String field = levelCodeFields[i].getName();
			Object filterValue = findValueByKey(filters, field);
			parentKeys[i] = filterValue == null ? null : filterValue.toString();
		}
		return parentKeys;
	}

	private void addSurveyFilter(List<NameValueEntry> filters, Survey survey) {
		if ( survey instanceof CollectSurvey ) {
			Integer surveyId = survey.getId();
			if ( surveyId != null ) {
				filters.add(new NameValueEntry(SURVEY_ID_FIELD, surveyId.toString()));
			}
		}
	}
	
	private Object findValueByKey(NameValueEntry[] nameValueEntries, String key) {
		for (int j = 0; j < nameValueEntries.length; j++) {
			NameValueEntry filter = nameValueEntries[j];
			String filterKey = filter.getKey();
			if (key.equals(filterKey)) {
				return filter.getValue();
			}
		}
		return null;
	}
	
	public void setDynamicTableDao(DynamicTableDao dynamicTableDao) {
		this.dynamicTableDao = dynamicTableDao;
	}
	
	public void setSamplingDesignDao(SamplingDesignDao samplingDesignDao) {
		this.samplingDesignDao = samplingDesignDao;
	}
}
