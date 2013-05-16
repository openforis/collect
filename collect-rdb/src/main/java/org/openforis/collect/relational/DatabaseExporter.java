package org.openforis.collect.relational;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.model.RelationalSchema;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public interface DatabaseExporter {
	
	void insertReferenceData(RelationalSchema schema) throws CollectRdbException;
	void insertData(RelationalSchema schema, CollectRecord record) throws CollectRdbException;
	
}
