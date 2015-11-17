package org.openforis.collect.relational.data;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.data.internal.CodeTableDataExtractor;
import org.openforis.collect.relational.data.internal.DataTableDataExtractor;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.DataTable;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExtractorFactory {

	public static CodeTableDataExtractor getExtractor(CodeTable table) {
		return new CodeTableDataExtractor(table);
	}
	
	public static DataExtractor getRecordDataExtractor(DataTable table, CollectRecord record) {
		return new DataTableDataExtractor(table, record);
	}
	
}
