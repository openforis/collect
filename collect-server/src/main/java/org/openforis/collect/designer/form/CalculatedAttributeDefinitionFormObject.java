/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Formula;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Type;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionFormObject<T extends CalculatedAttributeDefinition> extends AttributeDefinitionFormObject<T> {

	private String type;
	private List<Formula> formulas;
	
	CalculatedAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		reset();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setType(Type.valueOf(type));
		dest.setFormulas(formulas);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		Type typeEnum = source.getType();
		type = typeEnum.name();
		formulas = new ArrayList<CalculatedAttributeDefinition.Formula>(source.getFormulas());
	}

	@Override
	protected void reset() {
		super.reset();
		type = Type.DEFAULT.name();
		formulas = new ArrayList<CalculatedAttributeDefinition.Formula>();
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public List<Formula> getFormulas() {
		return formulas;
	}
	
	public void setFormulas(List<Formula> formulas) {
		this.formulas = formulas;
	}
	
}
