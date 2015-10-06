package org.openforis.collect.event;

import java.util.Collections;
import java.util.Date;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class RecordDeletedEvent extends RecordEvent {

	public RecordDeletedEvent(String surveyName, int recordId,
			RecordStep step, String definitionId, String nodeId,
			Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, Collections
				.<String> emptyList(), nodeId, timestamp, userName);
	}

}
