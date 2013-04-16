/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class RecordProxy implements Proxy {

	private transient CollectRecord record;
	private transient MessageContextHolder messageContextHolder;

	private Integer errors;
	private Integer skipped;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private Integer warnings;

	
	public RecordProxy(MessageContextHolder messageContextHolder, CollectRecord record) {
		this.record = record;
		this.messageContextHolder = messageContextHolder;
		errors = record.getErrors();
		skipped = record.getSkipped();
		missing = record.getMissing();
		missingErrors = record.getMissingErrors();
		missingWarnings = record.getMissingWarnings();
		warnings = record.getWarnings();
	}

	public static List<RecordProxy> fromList(MessageContextHolder messageContextHolder, List<CollectRecord> records) {
		List<RecordProxy> result = new ArrayList<RecordProxy>();
		if ( records != null ) {
			for (CollectRecord collectRecord : records) {
				RecordProxy proxy = new RecordProxy(messageContextHolder, collectRecord);
				result.add(proxy);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public Step getStep() {
		return record.getStep();
	}

	@ExternalizedProperty
	public State getState() {
		return record.getState();
	}

	@ExternalizedProperty
	public Date getCreationDate() {
		return record.getCreationDate();
	}

	@ExternalizedProperty
	public UserProxy getCreatedBy() {
		if(record.getCreatedBy() != null) {
			return new UserProxy(record.getCreatedBy());
		} else return null;
	}

	@ExternalizedProperty
	public Date getModifiedDate() {
		return record.getModifiedDate();
	}

	@ExternalizedProperty
	public Integer getId() {
		return record.getId();
	}

	@ExternalizedProperty
	public UserProxy getModifiedBy() {
		if(record.getModifiedBy() != null) {
			return new UserProxy(record.getModifiedBy());
		} else {
			return null;
		}
	}
	
	@ExternalizedProperty
	public EntityProxy getRootEntity() {
		if(record.getRootEntity() != null) {
			return new EntityProxy(messageContextHolder, null, record.getRootEntity());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public ModelVersionProxy getVersion() {
		if(record.getVersion() != null) {
			return new ModelVersionProxy(record.getVersion());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public List<String> getRootEntityKeys() {
		return record.getRootEntityKeyValues();
	}

	@ExternalizedProperty
	public List<Integer> getEntityCounts() {
		return record.getEntityCounts();
	}

	@ExternalizedProperty
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
	
	@ExternalizedProperty
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

}
