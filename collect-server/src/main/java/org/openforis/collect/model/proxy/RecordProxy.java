/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.model.Entity;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class RecordProxy implements Proxy {

	private transient CollectRecord record;
	private transient ProxyContext context;

	private boolean newRecord;
	private Integer errors;
	private Integer skipped;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private Integer warnings;
	private UserProxy owner;
	
	public RecordProxy(CollectRecord record, ProxyContext context) {
		this(record, context, false);
	}
	
	public RecordProxy(CollectRecord record, ProxyContext context, boolean newRecord) {
		this.record = record;
		this.context = context;
		this.newRecord = newRecord;
		
		errors = record.getErrors();
		skipped = record.getSkipped();
		missing = record.getMissing();
		missingErrors = record.getMissingErrors();
		missingWarnings = record.getMissingWarnings();
		/*missingErrors = missingWarnings = 0; //TODO these values are not stored in records table */
		warnings = record.getWarnings();
		owner = record.getOwner() == null ? null: new UserProxy(record.getOwner());
	}

	public static List<RecordProxy> fromList(List<CollectRecord> records, ProxyContext context) {
		List<RecordProxy> result = new ArrayList<RecordProxy>();
		if ( records != null ) {
			for (CollectRecord collectRecord : records) {
				result.add(new RecordProxy(collectRecord, context));
			}
		}
		return result;
	}
	
	public int getSurveyId() {
		return record.getSurvey().getId();
	}
	
	public boolean isNewRecord() {
		return newRecord;
	}
	
	public boolean isPreview() {
		return record.isPreview();
	}
	
	public Step getStep() {
		return record.getStep();
	}
	
	public Step getDataStep() {
		return record.getDataStep();
	}

	public State getState() {
		return record.getState();
	}

	public Date getCreationDate() {
		return record.getCreationDate();
	}

	public BasicUserProxy getCreatedBy() {
		User createdBy = record.getCreatedBy();
		return createdBy == null ? null: new BasicUserProxy(createdBy);
	}

	public Date getModifiedDate() {
		return record.getModifiedDate();
	}

	public Integer getId() {
		return record.getId();
	}

	public BasicUserProxy getModifiedBy() {
		User modifiedBy = record.getModifiedBy();
		return modifiedBy == null ? null: new BasicUserProxy(modifiedBy);
	}
	
	public EntityProxy getRootEntity() {
		Entity rootEntity = record.getRootEntity();
		return rootEntity == null ? null: new EntityProxy(null, record.getRootEntity(), context);
	}

	public Integer getVersionId() {
		ModelVersion version = record.getVersion();
		return version == null ? null : version.getId();
	}
	
	public List<String> getRootEntityKeys() {
		return record.getRootEntityKeyValues();
	}

	public List<Integer> getEntityCounts() {
		return record.getEntityCounts();
	}
	
	public List<String> getSummaryValues() {
		return record.getSummaryValues();
	}

	public boolean isEntryComplete() {
		if(record.getStep() != null) {
			switch(record.getStep()) {
			case ENTRY:
				return false;
			case CLEANSING:
			case ANALYSIS:
				return true;
			}
		}
		return false;
	}
	
	public boolean isCleansingComplete() {
		if(record.getStep() != null) {
			switch(record.getStep()) {
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
