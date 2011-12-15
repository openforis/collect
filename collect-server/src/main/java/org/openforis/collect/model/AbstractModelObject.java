/**
 * 
 */
package org.openforis.collect.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.model.ModelObject;

/**
 * @author M. Togna
 * 
 */
@MappedSuperclass
public abstract class AbstractModelObject<D extends ModelObjectDefinition> implements ModelObject<D> {

	@Column(unique = true, name = "id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	private D definition;
	private boolean relevant = true;
	private boolean required = false;

	private RecordImpl record;
	private ModelObject<? extends ModelObjectDefinition> parent;
	private String path;
	private String type;

	@Override
	public D getDefinition() {
		return this.definition;
	}

	@Override
	public boolean isRelevant() {
		return this.relevant;
	}

	@Override
	public boolean isRequired() {
		return this.required;
	}

	@Override
	public String getName() {
		return this.getDefinition().getName();
	}

	@Override
	public ModelObject<? extends ModelObjectDefinition> getParent() {
		return this.parent;
	}

	protected void setParent(ModelObject<? extends ModelObjectDefinition> parent) {
		this.parent = parent;
	}

	protected RecordImpl getRecord() {
		return this.record;
	}

	protected void setRecord(RecordImpl record) {
		this.record = record;
	}

	public String getPath() {
		return this.path;
	}

	void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public Long getId() {
		return id;
	}

}
