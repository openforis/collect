package org.openforis.collect.io.data;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeBatchProcessor;
import org.openforis.collect.model.NodeChangeSet;

public class FakeNodeChangeBatchProcessor implements NodeChangeBatchProcessor {

	private List<NodeChangeSet> changes = new ArrayList<NodeChangeSet>();

	@Override
	public void add(NodeChangeSet nodeChanges, String userName) {
		changes.add(nodeChanges);
	}

	@Override
	public void process(CollectRecord record) {
		changes.clear();
	}
	
	public List<NodeChangeSet> getChanges() {
		return changes;
	}

}
