/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.validation.Check;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefinitionFormObject<T extends AttributeDefinition> extends NodeDefinitionFormObject<T> {

	private List<AttributeDefault> attributeDefaults;
	private List<Check<?>> checks;

	AttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.removeAllAttributeDefaults();
		if ( attributeDefaults != null ) {
			for (AttributeDefault attrDefault : attributeDefaults) {
				dest.addAttributeDefault(attrDefault);
			}
		}
		dest.removeAllChecks();
		if ( checks != null ) {
			for (Check<?> check : checks) {
				dest.addCheck(check);
			}
		}
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		attributeDefaults = new ArrayList<AttributeDefault>(source.getAttributeDefaults());
		checks = new ArrayList<Check<?>>(source.getChecks());
	}
	
	@Override
	protected void reset() {
		super.reset();
		attributeDefaults = null;
		checks = null;
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public void setAttributeDefaults(List<AttributeDefault> attributeDefaults) {
		this.attributeDefaults = attributeDefaults;
	}

	public List<Check<?>> getChecks() {
		return checks;
	}

	public void setChecks(List<Check<?>> checks) {
		this.checks = checks;
	}
	
	
	
}
