package org.openforis.collect.model;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public interface NodeChangeBatchProcessor {

	void add(NodeChangeSet nodeChanges, String userName);

	void process(CollectRecord record);
}