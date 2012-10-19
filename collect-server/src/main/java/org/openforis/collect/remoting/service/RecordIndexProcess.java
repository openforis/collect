package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.RecordIndexException;
import org.openforis.collect.manager.RecordIndexManager;
import org.openforis.collect.model.CollectRecord;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordIndexProcess implements Runnable {

	private RecordIndexManager indexManager;
	private CollectRecord record;
	private boolean running;
	
	public RecordIndexProcess(RecordIndexManager indexManager, CollectRecord record) {
		super();
		this.indexManager = indexManager;
		this.record = record;
		this.running = false;
	}
	
	@Override
	public void run() {
		running = true;
		try {
			indexManager.index(record);
		} catch (RecordIndexException e) {
			throw new RuntimeException(e);
		}
		running = false;
	}
	
	public void cancel() {
		running = false;
		indexManager.cancelIndexing();
	}

	public boolean isRunning() {
		return running;
	}
	
}
