/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.File;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.Time;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 * */
public class AttributeProxy extends NodeProxy {

	private transient Attribute<? extends AttributeDefinition, ?> attribute;
	private ValidationResultsProxy validationResults;
	private boolean errorConfirmed;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AttributeProxy( EntityProxy parent, Attribute attribute) {
		super(parent, attribute);
		this.attribute = attribute;
		ValidationResults validationRes = attribute.validateValue();
		MessageSource messageSource = getMessageSource();
		validationResults = new ValidationResultsProxy(messageSource, attribute, validationRes);
		CollectRecord record = (CollectRecord) attribute.getRecord();
		errorConfirmed = record.isErrorConfirmed(attribute);
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
			} else if (val instanceof File) {
				return new FileProxy((File) val);
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
	
	public boolean isErrorConfirmed() {
		return errorConfirmed;
	}
	
	public void setErrorConfirmed(boolean value) {
		errorConfirmed = value;
	}

}
