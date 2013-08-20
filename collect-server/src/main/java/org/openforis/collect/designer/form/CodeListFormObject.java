package org.openforis.collect.designer.form;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListLabel;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListFormObject extends VersionableItemFormObject<CodeList> {

	public static final CodeScope DEFAULT_SCOPE = CodeScope.SCHEME;
	
	private String name;
	private String lookupTable;
	private String itemLabel;
	private String listLabel;
	private String description;
	private String type;
	private String codeScope;
	
	public enum Type {
		FLAT, HIERARCHICAL;
	}
	
	public CodeListFormObject() {
		type = Type.FLAT.name();
	}
	
	@Override
	public void loadFrom(CodeList source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		name = source.getName();
		lookupTable = source.getLookupTable();
		itemLabel = getLabel(source, CodeListLabel.Type.ITEM, languageCode, defaultLanguage);
		listLabel = getLabel(source, CodeListLabel.Type.LIST, languageCode, defaultLanguage);
		description = source.getDescription(languageCode);
		List<CodeListLevel> levels = source.getHierarchy();
		boolean hasMultipleLevels = levels.size() > 1;
		type = hasMultipleLevels ? Type.HIERARCHICAL.name(): Type.FLAT.name();
		CodeScope codeScopeEnum = source.getCodeScope();
		codeScope = codeScopeEnum != null ? codeScopeEnum.name(): DEFAULT_SCOPE.name();
	}
	
	@Override
	public void saveTo(CodeList dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setName(name);
		dest.setLookupTable(StringUtils.trimToNull(lookupTable));
		dest.setLabel(CodeListLabel.Type.ITEM, languageCode, itemLabel);
		dest.setLabel(CodeListLabel.Type.LIST, languageCode, listLabel);
		dest.setDescription(languageCode, description);
		CodeScope scope = StringUtils.isNotBlank(codeScope) ? CodeScope.valueOf(codeScope): DEFAULT_SCOPE;
		dest.setCodeScope(scope);
	}
	
	protected String getLabel(CodeList source, CodeListLabel.Type type, String languageCode, String defaultLanguage) {
		String result = source.getLabel(type, languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getLabel(type, null);
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLookupTable() {
		return lookupTable;
	}

	public void setLookupTable(String lookupTable) {
		this.lookupTable = lookupTable;
	}

	public String getItemLabel() {
		return itemLabel;
	}

	public void setItemLabel(String itemLabel) {
		this.itemLabel = itemLabel;
	}

	public String getListLabel() {
		return listLabel;
	}

	public void setListLabel(String listLabel) {
		this.listLabel = listLabel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCodeScope() {
		return codeScope;
	}

	public void setCodeScope(String codeScope) {
		this.codeScope = codeScope;
	}

	
}
