package org.openforis.idm.metamodel;

/**
 * A survey object that will be persisted in a database
 * (so it will have a unique identifier)
 * 
 * @author A. Modragon
 */
public abstract class PersistedSurveyObject extends SurveyObject {
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	protected PersistedSurveyObject(Survey survey) {
		super(survey);
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
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
