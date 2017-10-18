/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.collect.datacleansing.persistence.DataQueryDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component("dataQueryManager")
public class DataQueryManager extends AbstractSurveyObjectManager<DataQuery, DataQueryDao> {

	@Autowired
	private DataQueryTypeManager dataQueryTypeManager;
	@Autowired
	private DataQueryGroupManager dataQueryGroupManager;
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public void delete(DataQuery query) {
		checkNotExistsCalculationStepUsingQuery(query);
		checkNotExistsQueryGroup(query);
		super.delete(query);
	}
	
	@Override
	protected void initializeItem(DataQuery q) {
		super.initializeItem(q);
		initializeType(q);
	}
	
	public List<DataQuery> loadByType(DataQueryType type) {
		List<DataQuery> queries = dao.loadByType(type);
		initializeItems(queries);
		return queries;
	}
	
	private void initializeType(DataQuery q) {
		Integer typeId = q.getTypeId();
		DataQueryType type = typeId == null ? null : dataQueryTypeManager.loadById((CollectSurvey) q.getSurvey(), typeId);
		q.setType(type);
	}

	private void checkNotExistsCalculationStepUsingQuery(DataQuery query) {
		List<DataCleansingStep> steps = dataCleansingStepManager.loadByQuery(query);
		if (! steps.isEmpty()) {
			String message = messageSource.getMessage("data_query.delete.error.used_by_data_cleansing_step", new String[]{query.getTitle()}, Locale.ENGLISH);
			throw new IllegalStateException(message);
		}
	}
	
	private void checkNotExistsQueryGroup(DataQuery query) {
		Set<DataQueryGroup> groups = dataQueryGroupManager.loadByQuery(query);
		if (! groups.isEmpty()) {
			String queryCompleteTitle = query.getType().getCode() + " - " + query.getTitle();
			String message = messageSource.getMessage("data_query.delete.error.used_by_query_group", new String[]{queryCompleteTitle}, Locale.ENGLISH);
			throw new IllegalStateException(message);
		}
	}
		
	
}
