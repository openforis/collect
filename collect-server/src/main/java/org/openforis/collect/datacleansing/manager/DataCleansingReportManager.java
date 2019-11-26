/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingReport;
import org.openforis.collect.datacleansing.persistence.DataCleansingReportDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingReportManager extends AbstractSurveyObjectManager<Integer, DataCleansingReport, DataCleansingReportDao> {

	public List<DataCleansingReport> loadByCleansingChain(DataCleansingChain chain) {
		List<DataCleansingReport> items = dao.loadByCleansingChain(chain);
		initializeItems(items);
		return items;
	}
	
	public DataCleansingReport loadLastReport(DataCleansingChain chain) {
		return dao.loadLastReport(chain);
	}
	
}
