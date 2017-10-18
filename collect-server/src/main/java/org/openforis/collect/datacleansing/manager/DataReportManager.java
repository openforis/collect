/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.DataReport;
import org.openforis.collect.datacleansing.DataReportItem;
import org.openforis.collect.datacleansing.persistence.DataReportDao;
import org.openforis.collect.datacleansing.persistence.DataReportItemDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component("dataReportManager")
public class DataReportManager extends AbstractSurveyObjectManager<DataReport, DataReportDao> {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private DataQueryGroupManager queryGroupManager;
	@Autowired
	private DataReportItemDao dataReportItemDao;

	@Override
	public void delete(DataReport obj) {
		dataReportItemDao.deleteByReport(obj);
		super.delete(obj);
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dataReportItemDao.deleteBySurvey(survey);
		super.deleteBySurvey(survey);
	}
	
	public void saveItems(DataReport report, List<DataReportItem> items) {
		dataReportItemDao.insert(report, items);
	}
	
	public int countItems(DataReport report) {
		return dataReportItemDao.countItems(report);
	}
	
	public int countAffectedRecords(DataReport report) {
		return dataReportItemDao.countAffectedRecords(report);
	}
	
	public List<DataReportItem> loadItems(DataReport report) {
		return loadItems(report, null, null);
	}
	
	public List<DataReport> loadByQueryGroup(DataQueryGroup queryGroup) {
		List<DataReport> reports = dao.loadByQueryGroup(queryGroup);
		initializeItems(reports);
		return reports;
	}
	
	public List<DataReportItem> loadItems(DataReport report, Integer offset, Integer limit) {
		CollectSurvey survey = report.getSurvey();
		Map<Integer, CollectRecord> recordCache = new HashMap<Integer, CollectRecord>();
		List<DataReportItem> items = dataReportItemDao.loadByReport(report, offset, limit);
		for (DataReportItem item : items) {
			int recordId = item.getRecordId();
			CollectRecord record = recordCache.get(recordId);
			if (record == null) {
				record = recordManager.load(survey, recordId, report.getRecordStep(), false);
				recordCache.put(recordId, record);
			}
			item.setRecord(record);
		}
		return items;
	}

	@Override
	protected void initializeItem(DataReport i) {
		super.initializeItem(i);
		initQueryGroup(i);
	}
	
	private void initQueryGroup(DataReport report) {
		DataQueryGroup queryGroup = queryGroupManager.loadById(
				(CollectSurvey) report.getSurvey(), report.getQueryGroupId());
		report.setQueryGroup(queryGroup);
	}

}
