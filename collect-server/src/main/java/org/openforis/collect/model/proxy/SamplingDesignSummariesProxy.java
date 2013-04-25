package org.openforis.collect.model.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.SamplingDesignSummaries;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignSummariesProxy implements Proxy {

	private transient SamplingDesignSummaries summaries;
	
	public SamplingDesignSummariesProxy(SamplingDesignSummaries summaries) {
		super();
		this.summaries = summaries;
	}

	@ExternalizedProperty
	public List<SamplingDesignItemProxy> getRecords() {
		return SamplingDesignItemProxy.fromList(summaries.getRecords());
	}

	@ExternalizedProperty
	public int getTotalCount() {
		return summaries.getTotalCount();
	}
	
}
