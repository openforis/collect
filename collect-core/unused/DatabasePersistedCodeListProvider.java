/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.List;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class DatabasePersistedCodeListProvider implements
		CodeListService {

	@Autowired
	private CodeListItemDao codeListItemDao;
	
	/* (non-Javadoc)
	 * @see org.openforis.idm.metamodel.PersistedCodeListProvider#getRootItems(org.openforis.idm.metamodel.CodeList)
	 */
	@Override
	public List<PersistedCodeListItem> getRootItems(CodeList codeList) {
		return codeListItemDao.loadRootItems(codeList);
	}

	/* (non-Javadoc)
	 * @see org.openforis.idm.metamodel.PersistedCodeListProvider#getChildItems(org.openforis.idm.metamodel.PersistedCodeListItem)
	 */
	@Override
	public List<PersistedCodeListItem> getChildItems(
			PersistedCodeListItem parentItem) {
		CodeList codeList = parentItem.getCodeList();
		return codeListItemDao.loadChildItems(codeList, parentItem.getSystemId());
	}
	
	@Override
	public List<PersistedCodeListItem> loadItems(CodeList codeList, int level) {
		return codeListItemDao.loadItems(codeList, level);
	}
	
	@Override
	public boolean hasQualifiableItems(CodeList codeList) {
		return codeListItemDao.hasQualifiableItems(codeList);
	}
	
	@Override
	public PersistedCodeListItem getParentItem(PersistedCodeListItem item) {
		return codeListItemDao.loadById(item.getCodeList(), item.getParentId());
	}

}
