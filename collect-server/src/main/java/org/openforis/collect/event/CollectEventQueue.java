package org.openforis.collect.event;

import org.fao.foris.simpleeventbroker.EventBroker;

public class CollectEventQueue {
	
	private EventBroker queue;
	
	public CollectEventQueue(EventBroker queue) {
		super();
		this.queue = queue;
	}

	public void publish(RecordTransaction recordTransaction) {
		queue.publish(recordTransaction);
	}

}
