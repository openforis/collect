package org.openforis.idm.metamodel;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author G. Miceli
 */
public abstract class IdentifiableSurveyObject<I extends IdentifiableSurveyObject<I>> extends SurveyObject implements Comparable<I> {
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	protected IdentifiableSurveyObject(Survey survey, int id) {
		super(survey);
		this.id = id;
	}

	protected IdentifiableSurveyObject(Survey survey, IdentifiableSurveyObject<I> source, int id) {
		super(survey, source);
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
		IdentifiableSurveyObject<?> other = (IdentifiableSurveyObject<?>) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdentifiableSurveyObject<?> other = (IdentifiableSurveyObject<?>) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(I o) {
		return NumberUtils.compare(this.getId(), o.getId());
	}
	
}
