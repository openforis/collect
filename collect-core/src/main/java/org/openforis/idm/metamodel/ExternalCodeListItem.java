/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;
import java.util.Map;

import org.openforis.commons.collection.CollectionUtils;

/**
 * @author S. Ricci
 *
 */
public class ExternalCodeListItem extends CodeListItem {

	private static final long serialVersionUID = 1L;

	private Integer systemId;
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

	public Integer getSystemId() {
		return systemId;
	}

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((parentKeyByLevel == null) ? 0 : parentKeyByLevel.hashCode());
		result = prime * result
				+ ((systemId == null) ? 0 : systemId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalCodeListItem other = (ExternalCodeListItem) obj;
		if (parentKeyByLevel == null) {
			if (other.parentKeyByLevel != null)
				return false;
		} else if (!parentKeyByLevel.equals(other.parentKeyByLevel))
			return false;
		if (systemId == null) {
			if (other.systemId != null)
				return false;
		} else if (!systemId.equals(other.systemId))
			return false;
		return true;
	}

}
