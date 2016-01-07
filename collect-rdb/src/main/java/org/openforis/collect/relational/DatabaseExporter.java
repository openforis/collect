package org.openforis.collect.relational;

import java.io.Closeable;

import org.openforis.collect.model.CollectRecord;
import org.openforis.concurrency.ProgressListener;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public interface DatabaseExporter extends Closeable {
	
	void insertReferenceData(ProgressListener progressListener) throws CollectRdbException;
	void insertRecordData(CollectRecord record, ProgressListener progressListener) throws CollectRdbException;
	
}
