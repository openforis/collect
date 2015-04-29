package org.openforis.collect.datacleansing;

import java.util.Iterator;
import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryResultIterator implements Iterator<Node<?>> {

	private DataQueryEvaluator queryEvaluator;
	
	private RecordManager recordManager;
	
	private List<CollectRecord> recordSummaries;
	private int currentRecordIndex = 0;
	private CollectRecord currentRecord;
	private List<Node<?>> currentNodes;
	private int currentNodeIndex = 0;
	private boolean active = true;
	
	public DataQueryResultIterator(RecordManager recordManager, List<CollectRecord> recordSummaries, 
			DataQueryEvaluator queryEvaluator) {
		this.recordManager = recordManager;
		this.recordSummaries = recordSummaries;
		this.queryEvaluator = queryEvaluator;
	}

	@Override
	public boolean hasNext() {
		checkActive();
		if (currentRecordIndex > recordSummaries.size()) {
			return false;
		} else {
			if (currentRecord == null || currentNodeIndex >= currentNodes.size()) {
				fetchNextRecord();
			}
			return currentRecord != null;
		}
	}

	private void checkActive() {
		if (! active) {
			throw new IllegalStateException("The query result iterator has been deactivated.");
		}
	}

	@Override
	public Node<?> next() {
		if (hasNext()) {
			return currentNodes.get(currentNodeIndex ++);
		} else {
			return null;
		}
	}

	private void fetchNextRecord() {
		currentRecord = null;
		currentNodes = null;
		currentNodeIndex = 0;
		
		while (active && currentRecordIndex < recordSummaries.size()) {
			CollectRecord summary = recordSummaries.get(currentRecordIndex ++);
			CollectRecord record = recordManager.load((CollectSurvey) summary.getSurvey(), summary.getId());
			
			List<Node<?>> nodes = queryEvaluator.evaluate(record);
			if (! nodes.isEmpty()) {
				currentRecord = record;
				currentNodes = nodes;
			}
		}
	}

	public boolean isActive() {
		return active;
	}
	
	public void deactivate() {
		setActive(false);
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
