package org.openforis.idm.metamodel;

import java.util.Date;
import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;

/**
 * A survey object that will be persisted in a database
 * (so it will have a unique identifier)
 * 
 * @author A. Modragon
 */
public abstract class PersistedSurveyObject extends SurveyObject implements PersistedObject {
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private UUID uuid;
	private Date creationDate;
	private Date modifiedDate;
	
	protected PersistedSurveyObject(CollectSurvey survey) {
		this(survey, UUID.randomUUID());
	}

	protected PersistedSurveyObject(CollectSurvey survey, UUID uuid) {
		super(survey);
		this.uuid = uuid;
		this.creationDate = new Date();
	}
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersistedSurveyObject other = (PersistedSurveyObject) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
