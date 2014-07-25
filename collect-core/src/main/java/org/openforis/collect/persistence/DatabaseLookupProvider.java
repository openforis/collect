/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NameValueEntry;
import org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign;
import org.openforis.idm.metamodel.SamplingPoints;
import org.openforis.idm.metamodel.SamplingPoints.Attribute;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class DatabaseLookupProvider implements LookupProvider {

	private static final String SURVEY_ID_FIELD = "survey_id";
	private static final String SURVEY_WORK_ID_FIELD = "survey_work_id";

	@Autowired
	private DynamicTableDao dynamicTableDao;

	@Override
	public Object lookup(Survey survey, String name, String attribute, Object... columns) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		filters.addAll(Arrays.asList(NameValueEntry.fromKeyValuePairs(columns)));

		if ( survey instanceof CollectSurvey ) {
			CollectSurvey cs = (CollectSurvey) survey;
			filters.add(createSurveyFilter(cs));
			if ( name.equals(OfcSamplingDesign.OFC_SAMPLING_DESIGN.getName()) ) {
				convertSamplingDesignFilters(filters, cs);
				attribute = convertSamplingDesignAttributeName(survey, attribute);
			}
		}
		
		Object object = dynamicTableDao.loadValue(name, attribute, filters.toArray(new NameValueEntry[0]));
		return object;
	}
	
	private String convertSamplingDesignAttributeName(Survey survey, String attribute) {
		SamplingPoints samplingPoints = survey.getSamplingPoints();
		if ( samplingPoints != null ) {
			List<Attribute> infoAttributes = samplingPoints.getAttributes(false);
			for (int i = 0; i < infoAttributes.size(); i++) {
				Attribute infoAttr = infoAttributes.get(i);
				if ( infoAttr.getName().equals(attribute) ) {
					String newAttributeName = SamplingDesignDao.INFO_FIELDS[i].getName();
					return newAttributeName;
				}
			}
		}
		return attribute;
	}

	protected NameValueEntry createSurveyFilter(CollectSurvey survey) {
		Integer surveyId = survey.getId();
		if ( surveyId != null ) {
			String surveyIdFieldName = survey.isWork() ? SURVEY_WORK_ID_FIELD : SURVEY_ID_FIELD;
			NameValueEntry keyValue = new NameValueEntry(surveyIdFieldName, surveyId.toString());
			return keyValue;
		} else {
			return null;
		}
	}

	private void convertSamplingDesignFilters(List<NameValueEntry> filters, CollectSurvey cs) {
		SamplingPoints samplingPoints = cs.getSamplingPoints();
		if ( samplingPoints != null ) {
			List<Attribute> infoAttributes = samplingPoints.getAttributes(false);
			for (int i = 0; i < infoAttributes.size(); i++) {
				Attribute infoAttr = infoAttributes.get(i);
				NameValueEntry entry = getEntry(filters, infoAttr.getName());
				if ( entry != null ) {
					String newKey = SamplingDesignDao.INFO_FIELDS[i].getName();
					NameValueEntry newEntry = new NameValueEntry(newKey, entry.getValue());
					filters.remove(entry);
					filters.add(newEntry);
				}
			}
		}
	}

	private NameValueEntry getEntry(List<NameValueEntry> filters, String name) {
		for ( NameValueEntry entry : filters ) {
			if ( entry.getKey().equals(name) ) {
				return entry;
			}
		}
		return null;
	}

}
