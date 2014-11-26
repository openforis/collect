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
public class QueryResultIterator implements Iterator<Node<?>> {

	private QueryEvaluator queryEvaluator;
	
	private RecordManager recordManager;
	
	private List<CollectRecord> recordSummaries;
	private int currentRecordIndex = 0;
	private CollectRecord currentRecord;
	private List<Node<?>> currentNodes;
	private int currentNodeIndex = 0;
	
	public QueryResultIterator(RecordManager recordManager, List<CollectRecord> recordSummaries, 
			QueryEvaluator queryEvaluator) {
		this.recordManager = recordManager;
		this.recordSummaries = recordSummaries;
		this.queryEvaluator = queryEvaluator;
	}

	@Override
	public boolean hasNext() {
		if (currentRecordIndex > recordSummaries.size()) {
			return false;
		} else {
			if (currentRecord == null || currentNodeIndex >= currentNodes.size()) {
				fetchNextRecord();
			}
			return currentRecord != null;
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
		
		while (currentRecordIndex < recordSummaries.size()) {
			CollectRecord summary = recordSummaries.get(currentRecordIndex ++);
			CollectRecord record = recordManager.load((CollectSurvey) summary.getSurvey(), summary.getId());
			
			List<Node<?>> nodes = queryEvaluator.evaluate(record);
			if (! nodes.isEmpty()) {
				currentRecord = record;
				currentNodes = nodes;
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
