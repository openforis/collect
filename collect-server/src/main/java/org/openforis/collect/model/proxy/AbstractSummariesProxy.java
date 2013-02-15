package org.openforis.collect.model.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.AbstractSummaries;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractSummariesProxy implements Proxy {
	
	protected transient AbstractSummaries<?> summaries;

	public AbstractSummariesProxy(AbstractSummaries<?> summaries) {
		super();
		this.summaries = summaries;
	}

	@ExternalizedProperty
	public int getTotalCount() {
		return summaries.getTotalCount();
	}

	public abstract List<?> getRecords();

}
