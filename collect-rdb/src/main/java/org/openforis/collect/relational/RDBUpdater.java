package org.openforis.collect.relational;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.model.RelationalSchema;

/**
 * @author S. Ricci
 */
public interface RDBUpdater {
	
	void updateData(RelationalSchema schema, CollectRecord record) throws CollectRdbException;
	void deleteData(RelationalSchema schema, CollectRecord record) throws CollectRdbException;
	
}
