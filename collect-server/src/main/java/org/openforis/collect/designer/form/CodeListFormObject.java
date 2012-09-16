package org.openforis.collect.designer.form;

import java.util.List;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListFormObject extends VersionableItemFormObject<CodeList> {

	private String name;
	private String lookupTable;
	private String itemLabel;
	private String listLabel;
	private String description;
	private String type;
	
	public enum Type {
		FLAT, HIERARCHICAL;
	}
	
	public CodeListFormObject() {
		type = Type.FLAT.name();
	}
	
	@Override
	public void loadFrom(CodeList source, String languageCode) {
		super.loadFrom(source, languageCode);
		name = source.getName();
		lookupTable = source.getLookupTable();
		itemLabel = source.getLabel(org.openforis.idm.metamodel.CodeListLabel.Type.ITEM, languageCode);
		listLabel = source.getLabel(org.openforis.idm.metamodel.CodeListLabel.Type.LIST, languageCode);
		description = source.getDescription(languageCode);
		List<CodeListLevel> levels = source.getHierarchy();
		boolean hasMultipleLevels = levels.size() > 1;
		type = hasMultipleLevels ? Type.HIERARCHICAL.name(): Type.FLAT.name();
	}
	
	@Override
	public void saveTo(CodeList dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setName(name);
		dest.setLookupTable(lookupTable);
		dest.setLabel(org.openforis.idm.metamodel.CodeListLabel.Type.ITEM, languageCode, itemLabel);
		dest.setLabel(org.openforis.idm.metamodel.CodeListLabel.Type.LIST, languageCode, listLabel);
		dest.setDescription(languageCode, description);
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

	
}
