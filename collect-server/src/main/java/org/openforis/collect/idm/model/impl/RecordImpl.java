/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import java.util.Date;

import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.Record;

/**
 * @author M. Togna
 * 
 */
public class RecordImpl implements Record {

	private Entity rootEntity;
	private Date creationDate;
	private String createdBy;
	private Date modifiedDate;
	private String modifiedBy;
	private Long id;
	private ModelVersion version;
	private Survey survey;
	private ModelObjectListener listener;

	RecordImpl(Survey survey, ModelVersion version) {
		this.survey = survey;
		this.version = version;
		this.listener = new ModelObjectListener();
	}

	@Override
	public Survey getSurvey() {
		return this.survey;
	}

	@Override
	public Entity getRootEntity() {
		return this.rootEntity;
	}

	@Override
	public void setRootEntity(Entity rootEntity) {
		EntityImpl entityImpl = (EntityImpl) rootEntity;
		this.rootEntity = entityImpl;
		entityImpl.setRecord(this);
		entityImpl.setPath("/" + rootEntity.getDefinition().getName());
	}

	@Override
	public Date getCreationDate() {
		return this.creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String getCreatedBy() {
		return this.createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@Override
	public String getModifiedBy() {
		return this.modifiedBy;
	}

	@Override
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public ModelVersion getVersion() {
		return this.version;
	}

	@Override
	public void setVersion(ModelVersion version) {
		this.version = version;
	}

	protected void notifyListener(ModelObject<? extends ModelObjectDefinition> modelObject) {
		this.listener.onStateChange(modelObject);
	}
}
