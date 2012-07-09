package org.openforis.collect.remoting.service.dataImport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.proxy.RecordProxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummaryItemProxy implements Proxy {

	private transient DataImportSummaryItem item;
	
	public DataImportSummaryItemProxy(DataImportSummaryItem item) {
		this.item = item;
	}
	
	public static List<DataImportSummaryItemProxy> fromList(List<DataImportSummaryItem> items) {
		List<DataImportSummaryItemProxy> result = new ArrayList<DataImportSummaryItemProxy>();
		if ( items != null ) {
			for (DataImportSummaryItem item : items) {
				DataImportSummaryItemProxy proxy = new DataImportSummaryItemProxy(item);
				result.add(proxy);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public int getEntryId() {
		return item.getEntryId();
	}
	
	@ExternalizedProperty
	public RecordProxy getRecord() {
		return getRecordProxy(item.getRecord());
	}

	@ExternalizedProperty
	public RecordProxy getConflictingRecord() {
		return getRecordProxy(item.getConflictingRecord());
	}

	private RecordProxy getRecordProxy(CollectRecord record) {
		if ( record != null ) {
			return new RecordProxy(record);
		} else {
			return null;
		}
	}
	
	@ExternalizedProperty
	public Map<Step, List<String>> getWarnings() {
		return item.getWarnings();
	}

	@ExternalizedProperty
	public List<Step> getSteps() {
		return item.getSteps();
	}
	
	@ExternalizedProperty
	public boolean isEntryDataPresent() {
		Step step = Step.ENTRY;
		return hasStep(step);
	}

	@ExternalizedProperty
	public boolean isCleansingDataPresent() {
		Step step = Step.CLEANSING;
		return hasStep(step);
	}

	@ExternalizedProperty
	public boolean isAnalysisDataPresent() {
		Step step = Step.ANALYSIS;
		return hasStep(step);
	}

	protected boolean hasStep(Step step) {
		if ( item.getSteps() != null ) {
			return item.getSteps().contains(step);
		} else {
			return false;
		}
	}

}
