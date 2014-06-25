/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Type;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionFormObject<T extends CalculatedAttributeDefinition> extends AttributeDefinitionFormObject<T> {

	private String formula;
	private String type;
	
	CalculatedAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		type = Type.DEFAULT.name();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setFormula(formula);
		Type typeVal = Type.valueOf(type);
		dest.setType(typeVal);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		formula = source.getFormula();
		Type typeEnum = source.getType();
		type = typeEnum.name();
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
