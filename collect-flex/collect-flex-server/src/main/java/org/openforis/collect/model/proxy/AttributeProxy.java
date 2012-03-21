/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.Time;

/**
 * @author M. Togna
 * 
 */
public class AttributeProxy extends NodeProxy {

	private transient Attribute<? extends AttributeDefinition, ?> attribute;
	private ValidationResultsProxy validationResults;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AttributeProxy(Attribute attribute) {
		super(attribute);
		this.attribute = attribute;
		validationResults = new ValidationResultsProxy(attribute.validateValue());
	}

	@ExternalizedProperty
	public Object getValue() {
		Object val = attribute.getValue();
		if (val != null) {
			if (val instanceof Code) {
				return new CodeProxy((Code) val);
			} else if (val instanceof Coordinate) {
				return new CoordinateProxy((Coordinate) val);
			} else if (val instanceof Date) {
				return new DateProxy((Date) val);
			} else if (val instanceof TaxonOccurrence) {
				return new TaxonOccurrenceProxy((TaxonOccurrence) val);
			} else if (val instanceof Time) {
				return new TimeProxy((Time) val);
			} else {
				return val;
			}
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public boolean isEmpty() {
		return attribute.isEmpty();
	}

	public ValidationResultsProxy getValidationResults(){
		return validationResults;
	}

	public void setValidationResults(ValidationResultsProxy value) {
		validationResults = value;
	}

	@ExternalizedProperty
	public List<FieldProxy> getFields() {
		int fieldCount = attribute.getFieldCount();
		List<FieldProxy> result = new ArrayList<FieldProxy>(fieldCount);
		for (int i = 0; i < fieldCount; i++) {
			Field<?> field = attribute.getField(i);
			result.add(i, new FieldProxy(field));
		}
		return result;
	}
	
	@ExternalizedProperty
	public boolean isErrorConfirmed() {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		boolean errorConfirmed = record.isErrorConfirmed(attribute);
		return errorConfirmed;
	}	

}
