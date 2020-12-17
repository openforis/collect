package org.openforis.collect.remoting.service.dataimport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.Proxy;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.io.data.DataImportSummaryItem;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.proxy.RecordSummaryProxy;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;
import org.openforis.collect.utils.Numbers;

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
	
	public int getEntryId() {
		return item.getEntryId();
	}
	
	public RecordSummaryProxy getRecord() {
		return createRecordSummaryProxy(item.getRecordSummary(), locale);
	}
	
	public int getRecordTotalErrors() {
		return item.getRecordSummary() == null ? -1: Numbers.toInt(item.getRecordSummary().getCurrentStepSummary().getTotalErrors(), -1);
	}
	
	public int getRecordErrors() {
		return item.getRecordSummary() == null ? -1: Numbers.toInt(item.getRecordSummary().getCurrentStepSummary().getErrors(), -1);
	}
	
	public int getRecordMissingErrors() {
		return item.getRecordSummary() == null ? -1: Numbers.toInt(item.getRecordSummary().getCurrentStepSummary().getMissingErrors(), -1);
	}
	
	public Date getRecordCreationDate() {
		return item.getRecordSummary() == null ? null : item.getRecordSummary().getCreationDate();
	}
	
	public Date getRecordModifiedDate() {
		return item.getRecordSummary() == null ? null : item.getRecordSummary().getCreationDate();
	}
	
	public int getRecordCompletionPercent() {
		return item.getRecordCompletionPercent();
	}
	
	public int getRecordFilledAttributesCount() {
		return item.getRecordFilledAttributesCount();
	}

	public RecordSummaryProxy getConflictingRecord() {
		return createRecordSummaryProxy(item.getConflictingRecordSummary(), locale);
	}

	public int getConflictingRecordTotalErrors() {
		CollectRecordSummary conflictingRecord = item.getConflictingRecordSummary();
		return conflictingRecord == null ? -1 : Numbers.toInt(conflictingRecord.getCurrentStepSummary().getTotalErrors(), -1);
	}
	
	public int getConflictingRecordErrors() {
		CollectRecordSummary conflictingRecord = item.getConflictingRecordSummary();
		return conflictingRecord == null ? -1 : Numbers.toInt(conflictingRecord.getCurrentStepSummary().getErrors(), -1);
	}
	
	public int getConflictingRecordMissingErrors() {
		CollectRecordSummary conflictingRecord = item.getConflictingRecordSummary();
		return conflictingRecord == null ? -1 : Numbers.toInt(conflictingRecord.getCurrentStepSummary().getMissingErrors(), -1);
	}
	
	public int getConflictingRecordCompletionPercent() {
		return item.getConflictingRecordCompletionPercent();
	}
	
	public Date getConflictingRecordCreationDate() {
		return item.getConflictingRecordSummary() == null ? null : item.getConflictingRecordSummary().getCreationDate();
	}
	
	public Date getConflictingRecordModifiedDate() {
		return item.getConflictingRecordSummary() == null ? null : item.getConflictingRecordSummary().getCreationDate();
	}

	public Step getConflictingRecordStep() {
		return item.getConflictingRecordSummary() == null ? null : item.getConflictingRecordSummary().getStep();
	}
	
	public int getConflictingRecordFilledAttributesCount() {
		return item.getConflictingRecordFilledAttributesCount();
	}
	
	public int getCompletionDifferencePercent() {
		return item.calculateCompletionDifferencePercent();
	}

	public int getImportabilityLevel() {
		return item.calculateImportabilityLevel();
	}
	
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

	public List<Step> getSteps() {
		return item.getSteps();
	}
	
	public boolean isEntryDataPresent() {
		return hasStep(Step.ENTRY);
	}

	public boolean isCleansingDataPresent() {
		return hasStep(Step.CLEANSING);
	}

	public boolean isAnalysisDataPresent() {
		return hasStep(Step.ANALYSIS);
	}
	
	public boolean isConflictingRecordEntryDataPresent() {
		return isConflictingRecordAfterStep(Step.ENTRY);
	}

	public boolean isConflictingRecordCleansingDataPresent() {
		return isConflictingRecordAfterStep(Step.CLEANSING);
	}

	public boolean isConflictingRecordAnalysisDataPresent() {
		return isConflictingRecordAfterStep(Step.ANALYSIS);
	}

	private RecordSummaryProxy createRecordSummaryProxy(CollectRecordSummary summary, Locale locale) {
		if ( summary == null ) {
			return null;
		} else {
			ProxyContext context = new ProxyContext(locale, null, null);
			return new RecordSummaryProxy(summary, context);
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
		CollectRecordSummary conflictingRecord = item.getConflictingRecordSummary();
		return conflictingRecord != null && conflictingRecord.getStep().afterEqual(step);
	}

}
