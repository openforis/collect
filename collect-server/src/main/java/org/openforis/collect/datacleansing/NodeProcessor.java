package org.openforis.collect.datacleansing;

import org.openforis.idm.model.Node;

public abstract interface NodeProcessor {
	
	abstract void process(Node<?> node) throws Exception;
	
}