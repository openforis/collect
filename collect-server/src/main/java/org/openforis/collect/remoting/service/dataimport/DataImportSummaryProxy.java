package org.openforis.collect.remoting.service.dataimport;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.Proxy;
import org.openforis.collect.io.data.DataImportSummary;
import org.openforis.collect.io.data.DataImportSummary.FileErrorItem;
import org.openforis.collect.model.CollectRecord.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummaryProxy implements Proxy {

	private transient DataImportSummary summary;
	private Locale locale;

	public DataImportSummaryProxy(DataImportSummary summary, Locale locale) {
		super();
		this.summary = summary;
		this.locale = locale;
	}

	public List<FileUnmarshallingErrorProxy> getSkippedFileErrors() {
		List<FileErrorItem> skippedFileErrors = summary.getSkippedFileErrors();
		List<FileUnmarshallingErrorProxy> proxies = FileUnmarshallingErrorProxy.fromList(skippedFileErrors);
		return proxies;
	}

	public String getSurveyName() {
		return summary.getSurveyName();
	}

	public Map<Step, Integer> getTotalPerStep() {
		return summary.getTotalPerStep();
	}

	public List<DataImportSummaryItemProxy> getRecordsToImport() {
		return DataImportSummaryItemProxy.fromList(summary.getRecordsToImport(), locale);
	}

	public List<DataImportSummaryItemProxy> getConflictingRecords() {
		return DataImportSummaryItemProxy.fromList(summary.getConflictingRecords(), locale);
	}

}
