/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
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
	
	public NumericAttributeDefinitionFormObject() {
		type = NumericAttributeDefinition.Type.INTEGER.name();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		Type typeEnum = null;
		if ( type != null ) {
			typeEnum = NumericAttributeDefinition.Type.valueOf(type);
		}
		dest.setType(typeEnum);
		dest.removeAllPrecisionDefinitions();
		if ( precisions != null ) {
			for (Precision precision : precisions) {
				dest.addPrecisionDefinition(precision);
			}
		}
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		type = source.getType() != null ? source.getType().name(): null;
		precisions = new ArrayList<Precision>(source.getPrecisionDefinitions());
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
