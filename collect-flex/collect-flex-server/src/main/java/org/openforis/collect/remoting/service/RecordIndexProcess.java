package org.openforis.collect.remoting.service;

import java.util.concurrent.Callable;

import org.openforis.collect.manager.RecordIndexManager;
import org.openforis.collect.model.CollectRecord;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordIndexProcess implements Callable<Void> {

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
	public Void call() throws Exception {
		running = true;
		indexManager.index(record);
		running = false;
		return null;
	}
	
	public void cancel() {
		running = false;
		indexManager.cancelIndexing();
	}

	public boolean isRunning() {
		return running;
	}
	
}
