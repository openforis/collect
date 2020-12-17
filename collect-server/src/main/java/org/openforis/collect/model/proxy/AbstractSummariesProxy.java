package org.openforis.collect.model.proxy;

import java.util.List;

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

	public int getTotalCount() {
		return summaries.getTotalCount();
	}

	public abstract List<?> getRecords();

}
