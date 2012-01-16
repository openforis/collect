/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.Date;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
import org.openforis.collect.model.CollectRecord;

/**
 * @author M. Togna
 * 
 */
public class RecordProxy implements ModelProxy {
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
	public String getCreatedBy() {
		return record.getCreatedBy();
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
	public String getModifiedBy() {
		return record.getModifiedBy();
	}

	@ExternalizedProperty
	public EntityProxy getRootEntity() {
		return new EntityProxy(record.getRootEntity());
	}

	@ExternalizedProperty
	public ModelVersionProxy getVersion() {
		return new ModelVersionProxy(record.getVersion());
	}

	// public Node<? extends NodeDefinition> getNodeById(int id) {
	// return record.getNodeById(id);
	// }

}
