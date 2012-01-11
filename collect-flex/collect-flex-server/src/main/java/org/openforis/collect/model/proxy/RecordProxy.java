/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;

/**
 * @author M. Togna
 * 
 */
public class RecordProxy implements ModelProxy {

	private transient CollectRecord record;

	public RecordProxy(CollectRecord record) {
		this.record = record;
	}

}
