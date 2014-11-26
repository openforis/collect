package org.openforis.collect.datacleansing;

import org.openforis.idm.model.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author A. Modragon
 *
 */
@Component
public class DataErrorReportGenerator {
	
	@Autowired
	private DataQueryExecutor queryExecutor;
	
	public DataErrorReport generate(DataErrorQuery query){
		DataErrorReport report = new DataErrorReport();
		DataQueryResultIterator it = queryExecutor.execute(query);
		while (it.hasNext()) {
			Attribute<?, ?> attr = (Attribute<?, ?>) it.next();
			DataErrorReportItem item = new DataErrorReportItem();
			item.setReport(report);
			item.setRecordId(attr.getRecord().getId());
			item.setParentEntityId(attr.getParent().getInternalId());
			item.setValue(attr.getValue() == null ? null: attr.getValue().toString());
			report.addItem(item);
		}
		return report;
	}

}
