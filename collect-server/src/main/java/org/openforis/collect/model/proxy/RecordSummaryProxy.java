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
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author S. Ricci
 * 
 */
public class RecordSummaryProxy implements Proxy {

	private transient CollectRecordSummary summary;
	private transient Locale locale;

	private Integer errors;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private Integer skipped;
	private Integer warnings;
	private UserProxy owner;
	
	public RecordSummaryProxy(CollectRecordSummary summary, Locale locale) {
		this.summary = summary;
		this.locale = locale;
		
		errors = summary.getErrors();
		skipped = summary.getSkipped();
		missing = summary.getMissing();
//		missingErrors = summary.getMissingErrors();
//		missingWarnings = summary.getMissingWarnings();
		/*missingErrors = missingWarnings = 0; //TODO these values are not stored in records table */
//		warnings = summary.getWarnings();
//		owner = summary.getOwner() == null ? null: new UserProxy(summary.getOwner());
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

	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		return missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getWarnings() {
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public Integer getMissingErrors() {
		return missingErrors;
	}

	public void setMissingErrors(Integer missingErrors) {
		this.missingErrors = missingErrors;
	}

	public Integer getMissingWarnings() {
		return missingWarnings;
	}

	public void setMissingWarnings(Integer missingWarnings) {
		this.missingWarnings = missingWarnings;
	}

	public UserProxy getOwner() {
		return owner;
	}
	
	public void setOwner(UserProxy owner) {
		this.owner = owner;
	}
}
