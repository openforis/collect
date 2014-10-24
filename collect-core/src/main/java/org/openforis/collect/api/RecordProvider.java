package org.openforis.collect.api;

import org.openforis.idm.model.Record;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public interface RecordProvider {
	
	Record provideRecord(int recordId);

}
