package org.openforis.collect.remoting.service.dataImport;

import java.util.ArrayList;
import java.util.List;

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
	public List<Step> getSteps() {
		return item.getSteps();
	}

}
