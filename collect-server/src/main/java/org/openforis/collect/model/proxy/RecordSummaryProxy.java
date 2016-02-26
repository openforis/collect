/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.User;
import org.openforis.collect.utils.Proxies;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author S. Ricci
 * 
 */
public class RecordSummaryProxy implements Proxy {

	private transient CollectRecordSummary summary;
	private transient Locale locale;

	public RecordSummaryProxy(CollectRecordSummary summary, Locale locale) {
		this.summary = summary;
		this.locale = locale;
	}

	public static List<RecordSummaryProxy> fromList(List<CollectRecordSummary> summaries, Locale locale) {
		List<RecordSummaryProxy> result = new ArrayList<RecordSummaryProxy>();
		if ( summaries != null ) {
			for (CollectRecordSummary summary : summaries) {
				result.add(new RecordSummaryProxy(summary, locale));
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public Step getStep() {
		return summary.getStep();
	}

	@ExternalizedProperty
	public int getStepNumber() {
		return summary.getStep().getStepNumber();
	}
	
	@ExternalizedProperty
	public State getState() {
		return summary.getState();
	}

	@ExternalizedProperty
	public Date getCreationDate() {
		return summary.getCreationDate();
	}

	@ExternalizedProperty
	public UserProxy getCreatedBy() {
		User createdBy = summary.getCreatedBy();
		return createdBy == null ? null: new UserProxy(createdBy);
	}

	@ExternalizedProperty
	public Date getModifiedDate() {
		return summary.getModifiedDate();
	}

	@ExternalizedProperty
	public Integer getId() {
		return summary.getId();
	}

	@ExternalizedProperty
	public UserProxy getModifiedBy() {
		User modifiedBy = summary.getModifiedBy();
		return modifiedBy == null ? null: new UserProxy(modifiedBy);
	}
	
	@ExternalizedProperty
	public ModelVersionProxy getVersion() {
		ModelVersion version = summary.getVersion();
		return version == null ? null: new ModelVersionProxy(version);
	}

	@ExternalizedProperty
	public List<String> getRootEntityKeys() {
		return summary.getRootEntityKeyValues();
	}

	@ExternalizedProperty
	public List<Integer> getEntityCounts() {
		return summary.getEntityCounts();
	}

	@ExternalizedProperty
	public boolean isEntryComplete() {
		if(summary.getStep() != null) {
			switch(summary.getStep()) {
			case ENTRY:
				return false;
			case CLEANSING:
			case ANALYSIS:
				return true;
			}
		}
		return false;
	}
	
	@ExternalizedProperty
	public boolean isCleansingComplete() {
		if(summary.getStep() != null) {
			switch(summary.getStep()) {
				case ANALYSIS:
					return true;
				default:
					return false;
			}
		}
		return false;
	}

	@ExternalizedProperty
	public Integer getTotalErrors() {
		return summary.getTotalErrors();
	}
	
	@ExternalizedProperty
	public Integer getErrors() {
		return summary.getErrors();
	}

	@ExternalizedProperty
	public Integer getSkipped() {
		return summary.getSkipped();
	}

	@ExternalizedProperty
	public Integer getMissing() {
		return summary.getMissing();
	}

	@ExternalizedProperty
	public Integer getWarnings() {
		return summary.getMissingWarnings();
	}

	@ExternalizedProperty
	public Integer getMissingErrors() {
		return summary.getMissingErrors();
	}

	@ExternalizedProperty
	public Integer getMissingWarnings() {
		return summary.getMissingWarnings();
	}

	@ExternalizedProperty
	public UserProxy getOwner() {
		return Proxies.fromObject(summary.getOwner(), UserProxy.class);
	}
	
}
