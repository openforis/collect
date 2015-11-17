package org.openforis.collect.datacleansing;

import java.io.Closeable;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataQueryResultItemProcessor extends Closeable {
	
	void init() throws Exception;
	
	void process(DataQueryResultItem item) throws Exception;
	
}