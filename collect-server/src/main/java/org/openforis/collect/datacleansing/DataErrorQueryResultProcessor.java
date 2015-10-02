package org.openforis.collect.datacleansing;

import java.io.Closeable;

import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataErrorQueryResultProcessor extends Closeable {

	void init() throws Exception;

	void process(DataErrorQuery query, Node<?> node) throws Exception;

}
