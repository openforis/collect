package org.openforis.idm.metamodel;

import java.util.List;

import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;

/**
 * 
 * @author S. Ricci
 *
 */
public interface CodeListService {

	<T extends CodeListItem> T loadItem(CodeAttribute attribute);
	
	<T extends CodeListItem> List<T> loadRootItems(CodeList codeList);
	
	<T extends CodeListItem> T loadRootItem(CodeList list, String code, ModelVersion version);
	
	<T extends CodeListItem> List<T> loadChildItems(T parentItem);
	
	<T extends CodeListItem> List<T> loadItems(CodeList codeList, int level);
	
	boolean hasItems(Entity parent, CodeAttributeDefinition def);
	
	<T extends CodeListItem> List<T> loadValidItems(Entity parent, CodeAttributeDefinition def);

	<T extends CodeListItem> T loadParentItem(T item);
	
	boolean hasQualifiableItems(CodeList codeList);

	<T extends CodeListItem> void save(T item);

	<T extends CodeListItem> void save(List<T> items);

	<T extends CodeListItem> void delete( T item);
	
	void deleteAllItems(CodeList list);

}
