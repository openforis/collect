package org.openforis.idm.metamodel;


/**
 * @author G. Miceli
 */
public abstract class IdentifiableSurveyObject extends SurveyObject {
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	protected IdentifiableSurveyObject(Survey survey, int id) {
		super(survey);
		this.id = id;
	}

	protected IdentifiableSurveyObject(IdentifiableSurveyObject object, int id) {
		super(object);
		this.id = id;
	}
	
	public final int getId() {
		return id;
	}

	void setId(int id) {
		this.id = id;
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdentifiableSurveyObject other = (IdentifiableSurveyObject) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdentifiableSurveyObject other = (IdentifiableSurveyObject) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
