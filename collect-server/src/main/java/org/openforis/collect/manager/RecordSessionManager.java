package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 */
public class RecordSessionManager extends SessionManager implements EventListener {

//	private static Logger LOG = Logger.getLogger(RecordSessionManager.class);

	@Autowired
	private transient SessionRecordFileManager recordFileManager;
	
	private transient List<RecordEvent> pendingEvents = new CopyOnWriteArrayList<RecordEvent>();
	
	@Override
	public void onEvents(List<? extends RecordEvent> events) {
		//TODO filter events by active record
		pendingEvents.addAll(events);
	}
	
	@Override
	public void setActiveRecord(CollectRecord record) {
		super.setActiveRecord(record);
		recordFileManager.resetTempInfo();
	}

	@Override
	public void releaseRecord() throws RecordUnlockedException {
		super.releaseRecord();
		recordFileManager.deleteAllTempFiles();
		pendingEvents.clear();
	}
	
	public List<RecordEvent> flushPendingEvents() {
		if (pendingEvents.isEmpty()) {
			return Collections.emptyList();
		}
		List<RecordEvent> events = new ArrayList<RecordEvent>(pendingEvents);
		pendingEvents.clear();
		return events;
	}

	public boolean commitRecordFileChanges(CollectRecord record) throws RecordFileException {
		 return recordFileManager.commitChanges(record);
	}

	public void indexTempRecordFile(java.io.File tempFile, Integer nodeId) {
		recordFileManager.indexTempFile(nodeId, tempFile, tempFile.getName());
	}

	public void prepareDeleteTempRecordFile(CollectRecord record, Integer nodeId) {
		recordFileManager.prepareDeleteFile(record, nodeId);
	}
}
