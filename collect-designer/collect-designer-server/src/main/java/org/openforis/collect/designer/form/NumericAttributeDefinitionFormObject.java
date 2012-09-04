/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.List;

import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Precision;

/**
 * @author S. Ricci
 *
 */
public class NumericAttributeDefinitionFormObject<T extends NumericAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private String type;
	private List<Precision> precisions;
	
	@Override
	public void copyValues(T dest, String languageCode) {
		super.copyValues(dest, languageCode);
		Type typeEnum = NumericAttributeDefinition.Type.valueOf(type);
		dest.setType(typeEnum);
	}
	
	@Override
	public void setValues(T source, String languageCode) {
		super.setValues(source, languageCode);
		type = source.getType() != null ? source.getType().name(): null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Precision> getPrecisions() {
		return precisions;
	}

	public void setPrecisions(List<Precision> precisions) {
		this.precisions = precisions;
	}

	
}
