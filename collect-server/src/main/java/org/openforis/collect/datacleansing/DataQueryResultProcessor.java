package org.openforis.collect.datacleansing;

import java.io.Closeable;

import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataQueryResultProcessor extends Closeable {

	void init() throws Exception;

	void process(DataQuery query, Node<?> node) throws Exception;

}
