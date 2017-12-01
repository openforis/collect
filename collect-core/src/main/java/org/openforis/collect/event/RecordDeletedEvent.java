package org.openforis.collect.event;

import java.util.Date;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class RecordDeletedEvent extends RecordEvent {

	public RecordDeletedEvent(String surveyName, int recordId, Date timestamp, String userName) {
		super(surveyName, recordId, null, null, null, null, timestamp, userName);
	}

}
