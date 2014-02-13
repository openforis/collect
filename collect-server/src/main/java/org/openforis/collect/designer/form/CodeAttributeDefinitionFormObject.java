/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.annotation.DependsOn;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionFormObject<T extends CodeAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private boolean key;
	private CodeList list;
	private CodeAttributeDefinition parentCodeAttribute;
	private boolean strict;
	private boolean allowValuesSorting;
	
	CodeAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		strict = true;
		allowValuesSorting = false;
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setList(list);
		dest.setKey(key);
		dest.setAllowUnlisted(! strict);
		dest.setParentCodeAttributeDefinition(parentCodeAttribute);
		dest.setAllowValuesSorting(dest.isMultiple() && allowValuesSorting);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		key = source.isKey();
		list = source.getList();
		parentCodeAttribute = source.getParentCodeAttributeDefinition();
		strict = ! source.isAllowUnlisted();
		allowValuesSorting = source.isMultiple() && source.isAllowValuesSorting();
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public CodeList getList() {
		return list;
	}

	public void setList(CodeList list) {
		this.list = list;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
	public boolean isAllowValuesSorting() {
		return allowValuesSorting;
	}
	
	public void setAllowValuesSorting(boolean allowValuesSorting) {
		this.allowValuesSorting = allowValuesSorting;
	}
	
	public CodeAttributeDefinition getParentCodeAttribute() {
		return parentCodeAttribute;
	}
	
	public void setParentCodeAttribute(CodeAttributeDefinition parentCodeAttribute) {
		this.parentCodeAttribute = parentCodeAttribute;
	}

	@DependsOn("parentCodeAttribute")
	public String getParentCodeAttributePath() {
		return parentCodeAttribute == null ? null: parentCodeAttribute.getPath();
	}
	
	@DependsOn("list")
	public boolean isHierarchicalList() {
		return list != null && list.isHierarchical();
	}
}
