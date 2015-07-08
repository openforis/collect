package org.openforis.collect.io.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fao.foris.simpleeventbroker.EventBroker;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeBatchProcessor;
import org.openforis.collect.model.NodeChangeSet;

public class RecordEventBatchPublisher implements EventListener, NodeChangeBatchProcessor {

	private List<RecordEvent> events = new ArrayList<RecordEvent>();
	private EventProducer eventProducer = new EventProducer(Arrays.<EventListener>asList(this));
	private EventBroker eventQueue;
	
	public RecordEventBatchPublisher(EventBroker eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	@Override
	public void onEvents(List<? extends RecordEvent> events) {
		this.events.addAll(events);
	}
	
	public void add(NodeChangeSet nodeChanges, String userName) {
		eventProducer.produceFor(nodeChanges, userName);
	}
	
	public void process(CollectRecord record) {
		if (! events.isEmpty()) {
			initializeEvents(record);
			String surveyName = record.getSurvey().getName();
			eventQueue.publish(new RecordTransaction(surveyName, record.getId(), record.getStep().toRecordStep(), events));
			events.clear();
		}
	}

	private void initializeEvents(CollectRecord record) {
		for (RecordEvent recordEvent : events) {
			//TODO make RecordEvent.recordId final (assign recordId earlier)
			if (recordEvent.getRecordId() == null) {
				recordEvent.initializeRecordId(record.getId());
			}
		}
	}
	
}
