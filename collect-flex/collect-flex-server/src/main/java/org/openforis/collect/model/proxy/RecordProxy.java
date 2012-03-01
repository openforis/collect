/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.Date;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
import org.openforis.collect.model.CollectRecord;

/**
 * @author M. Togna
 * 
 */
public class RecordProxy implements Proxy {
	public enum Step {
		ENTRY(1), CLEANSING(2), ANALYSIS(3);

		private int stepNumber;

		private Step(int stepNumber) {
			this.stepNumber = stepNumber;
		}

		public int getStepNumber() {
			return stepNumber;
		}

		public static Step valueOf(int stepNumber) {
			switch (stepNumber) {
				case 1:
					return ENTRY;
				case 2:
					return CLEANSING;
				case 3:
					return ANALYSIS;
				default:
					throw new IllegalArgumentException("Invalid step number");
			}
		}
	}

	private transient CollectRecord record;

	public RecordProxy(CollectRecord record) {
		this.record = record;
	}

	@ExternalizedProperty
	public boolean isSubmitted() {
		return record.isSubmitted();
	}

	@ExternalizedProperty
	public Step getStep() {
		return Step.valueOf(record.getStep().getStepNumber());
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
			return new EntityProxy(record.getRootEntity());
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
		return record.getRootEntityKeys();
	}

	@ExternalizedProperty
	public List<Integer> getEntityCounts() {
		return record.getEntityCounts();
	}

	@ExternalizedProperty
	public boolean isEntryComplete() {
		if(record.getStep() != null) {
			switch(record.getStep()) {
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
			}
		}
		return false;
	}

}
