package org.openforis.collect.event;

import java.util.ArrayList;
import java.util.List;

public class EventListenerToList implements EventListener {

	private final List<RecordEvent> list;

	public EventListenerToList() {
		this(null);
	}
	
	public EventListenerToList(List<RecordEvent> list) {
		this.list = list == null ? new ArrayList<RecordEvent>() : list;
	}
	
	@Override
	public void onEvent(RecordEvent event) {
		list.add(event);
	}
	
	public List<RecordEvent> getList() {
		return list;
	}
}