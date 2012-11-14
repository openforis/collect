/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.List;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Precision;

/**
 * @author S. Ricci
 *
 */
public class NumericAttributeDefinitionFormObject<T extends NumericAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private String type;
	private List<PrecisionFormObject> precisions;
	
	NumericAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
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
			for (PrecisionFormObject precisionFormObject : precisions) {
				Precision precision = new Precision();
				precisionFormObject.saveTo(precision, languageCode);
				dest.addPrecisionDefinition(precision);
			}
		}
	}

	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		type = source.getType() != null ? source.getType().name(): null;
		precisions = PrecisionFormObject.fromList(source.getPrecisionDefinitions(), languageCode, defaultLanguage);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<PrecisionFormObject> getPrecisions() {
		return precisions;
	}

	public void setPrecisions(List<PrecisionFormObject> precisions) {
		this.precisions = precisions;
	}

}
