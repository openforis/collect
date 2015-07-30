package org.openforis.collect.relational;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.concurrency.ProgressListener;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public interface DatabaseExporter {
	
	void insertReferenceData(RelationalSchema schema, ProgressListener progressListener) throws CollectRdbException;
	void insertRecordData(RelationalSchema schema, CollectRecord record, ProgressListener progressListener) throws CollectRdbException;
	
}
