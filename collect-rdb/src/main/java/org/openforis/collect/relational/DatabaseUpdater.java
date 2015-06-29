package org.openforis.collect.relational;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.model.RelationalSchema;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public interface DatabaseUpdater {
	
	void updateData(RelationalSchema schema, CollectRecord record) throws CollectRdbException;
	void deleteData(RelationalSchema schema, CollectRecord record) throws CollectRdbException;
	
}
