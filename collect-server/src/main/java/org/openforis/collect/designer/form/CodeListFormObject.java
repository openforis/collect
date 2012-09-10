package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLabel.Type;

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
	
	@Override
	public void loadFrom(CodeList source, String languageCode) {
		super.loadFrom(source, languageCode);
		name = source.getName();
		lookupTable = source.getLookupTable();
		itemLabel = source.getLabel(Type.ITEM, languageCode);
		listLabel = source.getLabel(Type.LIST, languageCode);
		description = source.getDescription(languageCode);
	}
	
	@Override
	public void saveTo(CodeList dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setName(name);
		dest.setLookupTable(lookupTable);
		dest.setLabel(Type.ITEM, languageCode, itemLabel);
		dest.setLabel(Type.LIST, languageCode, listLabel);
		dest.setDescription(languageCode, description);
	}

}
