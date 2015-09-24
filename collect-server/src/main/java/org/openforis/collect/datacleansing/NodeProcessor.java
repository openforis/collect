package org.openforis.collect.datacleansing;

import java.io.Closeable;

import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract interface NodeProcessor extends Closeable {
	
	void init() throws Exception;
	
	void process(Node<?> node) throws Exception;
	
}