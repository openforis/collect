package org.openforis.collect.remoting.service.dataImport;

import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummaryProxy implements Proxy {

	private transient DataImportSummary summary;

	public DataImportSummaryProxy(DataImportSummary summary) {
		super();
		this.summary = summary;
	}

	@ExternalizedProperty
	public Map<String, String> getSkippedFileErrors() {
		return summary.getSkippedFileErrors();
	}

	@ExternalizedProperty
	public boolean isNewSurvey() {
		return summary.isNewSurvey();
	}

	@ExternalizedProperty
	public Map<Step, Integer> getTotalPerStep() {
		return summary.getTotalPerStep();
	}

	@ExternalizedProperty
	public List<DataImportSummaryItemProxy> getRecordsToImport() {
		return DataImportSummaryItemProxy.fromList(summary.getRecordsToImport());
	}

	@ExternalizedProperty
	public List<DataImportSummaryItemProxy> getConflictingRecords() {
		return DataImportSummaryItemProxy.fromList(summary.getConflictingRecords());
	}
	
}
