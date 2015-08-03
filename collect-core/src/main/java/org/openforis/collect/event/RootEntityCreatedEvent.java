package org.openforis.collect.event;

import java.util.Collections;
import java.util.Date;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class RootEntityCreatedEvent extends EntityCreatedEvent {

	public RootEntityCreatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, String rootEntityId,
			Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, Collections
				.<String> emptyList(), rootEntityId, timestamp, userName);
	}

}
