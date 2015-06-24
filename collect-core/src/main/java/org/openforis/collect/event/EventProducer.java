package org.openforis.collect.event;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.springframework.stereotype.Component;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
@Component
public class EventProducer implements EventSource {

	private final List<EventListener> listeners = new ArrayList<EventListener>();

	@Override
	public void register(EventListener listener) {
		listeners.add(listener);
	}

	public void produceFor(NodeChangeSet changeSet) {
		
	}

	public void produceForNew(CollectRecord record) {
		
	}

	public void produceForDeleted(CollectRecord record) {
		
	}
	
	

}
