package org.openforis.collect.remoting.service.dataimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.io.data.DataImportSummaryItem;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.proxy.RecordSummaryProxy;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummaryItemProxy implements Proxy {

	private transient DataImportSummaryItem item;
	private transient Locale locale;
	
	public DataImportSummaryItemProxy(DataImportSummaryItem item, Locale locale) {
		this.item = item;
		this.locale = locale;
	}
	
	public static List<DataImportSummaryItemProxy> fromList(List<DataImportSummaryItem> items, Locale locale) {
		List<DataImportSummaryItemProxy> result = new ArrayList<DataImportSummaryItemProxy>();
		if ( items != null ) {
			for (DataImportSummaryItem item : items) {
				DataImportSummaryItemProxy proxy = new DataImportSummaryItemProxy(item, locale);
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
	public RecordSummaryProxy getRecord() {
		return createRecordSummaryProxy(item.getRecord(), locale);
	}
	
	@ExternalizedProperty
	public int getRecordErrors() {
		return item.getRecord().getErrors();
	}
	
	@ExternalizedProperty
	public int getRecordCompletionPercent() {
		return item.getRecordCompletionPercent();
	}
	
	@ExternalizedProperty
	public int getRecordFilledAttributesCount() {
		return item.getRecordFilledAttributesCount();
	}

	@ExternalizedProperty
	public RecordSummaryProxy getConflictingRecord() {
		return createRecordSummaryProxy(item.getConflictingRecord(), locale);
	}

	@ExternalizedProperty
	public int getConflictingRecordErrors() {
		return item.getConflictingRecord().getErrors();
	}
	
	@ExternalizedProperty
	public int getConflictingRecordCompletionPercent() {
		return item.getConflictingRecordCompletionPercent();
	}

	@ExternalizedProperty
	public int getConflictingRecordFilledAttributesCount() {
		return item.getConflictingRecordFilledAttributesCount();
	}
	
	@ExternalizedProperty
	public int getCompletionDifferencePercent() {
		return item.calculateCompletionDifferencePercent();
	}

	@ExternalizedProperty
	public int getImportabilityLevel() {
		return item.calculateImportabilityLevel();
	}

	@ExternalizedProperty
	public List<NodeUnmarshallingErrorProxy> getWarnings() {
		List<NodeUnmarshallingError> result = new ArrayList<NodeUnmarshallingError>();
		Map<Step, List<NodeUnmarshallingError>> warnings = item.getWarnings();
		if ( warnings != null ) {
			Set<Step> steps = warnings.keySet();
			for (Step step : steps) {
				List<NodeUnmarshallingError> warningsPerStep = warnings.get(step);
				result.addAll(warningsPerStep);
			}
		}
		List<NodeUnmarshallingErrorProxy> proxies = NodeUnmarshallingErrorProxy.fromList(result);
		return proxies;
	}

	@ExternalizedProperty
	public List<Step> getSteps() {
		return item.getSteps();
	}
	
	@ExternalizedProperty
	public boolean isEntryDataPresent() {
		return hasStep(Step.ENTRY);
	}

	@ExternalizedProperty
	public boolean isCleansingDataPresent() {
		return hasStep(Step.CLEANSING);
	}

	@ExternalizedProperty
	public boolean isAnalysisDataPresent() {
		return hasStep(Step.ANALYSIS);
	}
	
	@ExternalizedProperty
	public boolean isConflictingRecordEntryDataPresent() {
		return isConflictingRecordAfterStep(Step.ENTRY);
	}

	@ExternalizedProperty
	public boolean isConflictingRecordCleansingDataPresent() {
		return isConflictingRecordAfterStep(Step.CLEANSING);
	}

	@ExternalizedProperty
	public boolean isConflictingRecordAnalysisDataPresent() {
		return isConflictingRecordAfterStep(Step.ANALYSIS);
	}

	private RecordSummaryProxy createRecordSummaryProxy(CollectRecordSummary summary, Locale locale) {
		if ( summary == null ) {
			return null;
		} else {
			return new RecordSummaryProxy(summary, locale);
		}
	}
	
	protected boolean hasStep(Step step) {
		if ( item.getSteps() != null ) {
			return item.getSteps().contains(step);
		} else {
			return false;
		}
	}
	
	private boolean isConflictingRecordAfterStep(Step step) {
		return item.getConflictingRecord().getStep().afterEqual(step);
	}

	
}
