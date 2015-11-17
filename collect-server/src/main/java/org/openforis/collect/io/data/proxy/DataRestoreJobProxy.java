/**
 * 
 */
package org.openforis.collect.io.data.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.utils.Proxies;
import org.openforis.concurrency.proxy.JobProxy;

/**
 * @author S. Ricci
 *
 */
public class DataRestoreJobProxy extends JobProxy {

	public DataRestoreJobProxy(DataRestoreJob job) {
		super(job);
	}
	
	@ExternalizedProperty
	public List<RecordImportErrorProxy> getErrors() {
		return Proxies.fromList(((DataRestoreJob) job).getErrors(), RecordImportErrorProxy.class);
	}

}
