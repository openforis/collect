/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openforis.commons.collection.CollectionUtils;

/**
 * @author S. Ricci
 *
 */
public class ExternalCodeListItem extends PersistedCodeListItem {

	private static final long serialVersionUID = 1L;

	private Map<String, String> parentKeyByLevel;
	
	public ExternalCodeListItem(CodeList codeList, int itemId, int level) {
		super(codeList, itemId, level);
	}
	
	public ExternalCodeListItem(CodeList codeList, int itemId, Map<String, String> parentKeyByLevel, int level) {
		this(codeList, itemId, level);
		this.parentKeyByLevel = parentKeyByLevel;
	}
	
	@Override
	public CodeListItem getParentItem() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends CodeListItem> List<T> getChildItems() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CodeListItem getChildItem(String code) {
		throw new UnsupportedOperationException();
	}
	
	public Map<String, String> getParentKeyByLevel() {
		return CollectionUtils.unmodifiableMap(parentKeyByLevel);
	}
	
	public int getLevel() {
		return getParentKeyByLevel().size() + 1;
	}
	
	public List<String> getParentKeys() {
		if (parentKeyByLevel == null) {
			return Collections.emptyList();
		} else {
			int size = parentKeyByLevel.size();
			List<String> parentKeys = new ArrayList<String>(size);
			for(int ancestorLevelIndex = 0; ancestorLevelIndex < size; ancestorLevelIndex ++ ) {
				String ancestorLevelName = getCodeList().getHierarchy().get(ancestorLevelIndex).getName();
				String ancestorKey = parentKeyByLevel.get(ancestorLevelName);
				parentKeys.add(ancestorKey);
			}
			return parentKeys;
		}
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalCodeListItem other = (ExternalCodeListItem) obj;
		if (parentKeyByLevel == null) {
			if (other.parentKeyByLevel != null)
				return false;
		} else if (!parentKeyByLevel.equals(other.parentKeyByLevel))
			return false;
		return true;
	}

}
