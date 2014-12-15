/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorReportItem;
import org.openforis.collect.datacleansing.persistence.DataErrorReportDao;
import org.openforis.collect.datacleansing.persistence.DataErrorReportItemDao;
import org.openforis.collect.manager.AbstractPersistedObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataErrorReportManager extends AbstractPersistedObjectManager<DataErrorReport, DataErrorReportDao> {

	@Autowired
	private DataErrorQueryManager errorQueryManager;
	@Autowired
	private DataErrorReportItemDao errorReportItemDao;

	@Autowired
	@Override
	public void setDao(DataErrorReportDao dao) {
		super.setDao(dao);
	}
	
	public DataErrorReport loadById(CollectSurvey survey, int id) {
		DataErrorReport report = super.loadById(id);
		DataErrorQuery query = errorQueryManager.loadById(survey, report.getQueryId());
		report.setQuery(query);
		List<DataErrorReportItem> items = errorReportItemDao.loadByReport(report);
		report.setItems(items);
		return report;
	}
	
	public void saveItems(DataErrorReport report, List<DataErrorReportItem> items) {
		errorReportItemDao.insert(report, items);
	}
	
	public List<DataErrorReportItem> loadItems(DataErrorReport report) {
		return errorReportItemDao.loadByReport(report);
	}
	
}
