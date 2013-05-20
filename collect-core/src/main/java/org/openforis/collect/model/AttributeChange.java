package org.openforis.collect.model;

import java.util.Map;

import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;

/**
 * Change related to an Attribute.
 * It includes validation results and values of fields that have been updated.
 * 
 * @author S. Ricci
 *
 */
public class AttributeChange extends NodeChange<Attribute<?, ?>> {
	
	private ValidationResults validationResults;
	private Map<Integer, Object> updatedFieldValues;
	
	public AttributeChange(Attribute<?, ?> node) {
		super(node);
	}

	public void merge(AttributeChange newChange) {
		if ( updatedFieldValues == null ) {
			updatedFieldValues = newChange.updatedFieldValues;
		} else {
			updatedFieldValues.putAll(newChange.updatedFieldValues);
		}
		this.validationResults = newChange.validationResults;
	}

	public Map<Integer, Object> getUpdatedFieldValues() {
		return updatedFieldValues;
	}

	public void setUpdatedFieldValues(Map<Integer, Object> updatedFieldValues) {
		this.updatedFieldValues = updatedFieldValues;
	}

	public ValidationResults getValidationResults() {
		return validationResults;
	}

	public void setValidationResults(
			ValidationResults validationResults) {
		this.validationResults = validationResults;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((updatedFieldValues == null) ? 0 : updatedFieldValues
						.hashCode());
		result = prime
				* result
				+ ((validationResults == null) ? 0 : validationResults
						.hashCode());
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
		AttributeChange other = (AttributeChange) obj;
		if (updatedFieldValues == null) {
			if (other.updatedFieldValues != null)
				return false;
		} else if (!updatedFieldValues.equals(other.updatedFieldValues))
			return false;
		if (validationResults == null) {
			if (other.validationResults != null)
				return false;
		} else if (!validationResults.equals(other.validationResults))
			return false;
		return true;
	}
	
}