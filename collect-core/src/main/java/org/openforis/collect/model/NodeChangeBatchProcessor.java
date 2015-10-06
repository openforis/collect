package org.openforis.collect.model;


public interface NodeChangeBatchProcessor {

	void add(NodeChangeSet nodeChanges, String userName);

	void process(CollectRecord record);
}