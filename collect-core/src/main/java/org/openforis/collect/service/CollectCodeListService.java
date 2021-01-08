/**
 * 
 */
package org.openforis.collect.service;

import java.util.List;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class CollectCodeListService implements CodeListService {

	@Autowired
	private CodeListManager codeListManager;
	
	@Override
	public <T extends CodeListItem> List<T> loadRootItems(
			CodeList codeList) {
		return codeListManager.loadRootItems(codeList);
	}

	@Override
	public <T extends CodeListItem> List<T> loadChildItems(T parentItem) {
		return codeListManager.loadChildItems(parentItem);
	}

	@Override
	public <T extends CodeListItem> List<T> loadItems(CodeList codeList, int level) {
		return codeListManager.loadItems(codeList, level);
	}
	
	@Override
	public <T extends CodeListItem> List<T> loadValidItems(Entity parent,
			CodeAttributeDefinition def) {
		return codeListManager.loadValidItems(parent, def);
	}
	
	@Override
	public boolean hasItems(Entity parent, CodeAttributeDefinition def) {
		return codeListManager.hasItems(parent, def);
	}
	
	@Override
	public <T extends CodeListItem> T loadParentItem(T item) {
		return codeListManager.loadParentItem(item);
	}

	@Override
	public boolean hasQualifiableItems(CodeList codeList) {
		return codeListManager.hasQualifiableItems(codeList);
	}

	@Override
	public <T extends CodeListItem> void save(T item) {
		if ( item instanceof PersistedCodeListItem ) {
			codeListManager.save((PersistedCodeListItem) item);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> void save(List<T> items) {
		codeListManager.save((List<PersistedCodeListItem>) items);
	}
	
	@Override
	public <T extends CodeListItem> void delete(T item) {
		if ( item instanceof PersistedCodeListItem ) {
			codeListManager.delete((PersistedCodeListItem) item);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void deleteAllItems(CodeList list) {
		codeListManager.deleteAllItems(list);
	}

	@Override
	public <T extends CodeListItem> T loadItem(CodeAttribute attribute) {
		return codeListManager.loadItemByAttribute(attribute);
	}

	@Override
	public <T extends CodeListItem> T loadRootItem(CodeList list, String code,
			ModelVersion version) {
		return codeListManager.loadRootItem(list, code, version);
	}

	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
}
