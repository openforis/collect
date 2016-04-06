package org.openforis.collect.reporting.proxy;

import java.util.Date;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.reporting.ReportingRepositoryInfo;

public class ReportingRepositoryInfoProxy implements Proxy {
	
	private transient ReportingRepositoryInfo info;
	
	public ReportingRepositoryInfoProxy(ReportingRepositoryInfo info) {
		this.info = info;
	}
	
	@ExternalizedProperty
	public Date getLastUpdate() {
		return info.getLastUpdate();
	}

	@ExternalizedProperty
	public int getUpdatedRecordsSinceLastUpdate() {
		return info.getUpdatedRecordsSinceLastUpdate();
	}

}
