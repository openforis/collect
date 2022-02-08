package org.openforis.collect.relational;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.data.ColumnValuePair;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.concurrency.ProgressListener;

/**
 * @author S. Ricci
 */
public interface RDBUpdater extends Closeable {
	
	void insertEntity(int recordId, 
			Integer parentId, int entityId,
			int entityDefinitionId);

	void insertAttribute(int recordId, 
			Integer parentId, int attributeId,
			int attributeDefinitionId);

	void replaceRecordData(CollectRecord record, ProgressListener progressListener);
	
	void updateEntityData(DataTable dataTable,
			BigInteger pkValue,
			List<ColumnValuePair<DataColumn, ?>> columnValuePairs);
	
	void deleteRecordData(int recordId, int rootDefId);

	void deleteEntity(int recordId, int entityId, int definitionId);
	
	void deleteAttribute(int recordId, int attributeId, int definitionId);

}
