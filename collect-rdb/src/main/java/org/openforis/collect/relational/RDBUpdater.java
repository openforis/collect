package org.openforis.collect.relational;

import java.math.BigInteger;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.data.ColumnValuePair;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;

/**
 * @author S. Ricci
 */
public interface RDBUpdater {
	
	void updateData(RelationalSchema schema, CollectRecord record);
	
	void updateData(RelationalSchema rdbSchema, DataTable dataTable,
			BigInteger pkValue,
			List<ColumnValuePair<DataColumn, ?>> columnValuePairs);
	
	void deleteData(RelationalSchema schema, int recordId, int rootDefId);

	void deleteDataForEntity(RelationalSchema schema, int recordId,
			int entityId, int entityDefinitionId);

}
