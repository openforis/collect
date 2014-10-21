/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;


/**
 * @author S. Ricci
 *
 */
public class PersistedCodeListItem extends CodeListItem {

	private static final long serialVersionUID = 1L;

	private Integer systemId;
	private Integer parentId;
	private Integer sortOrder;
	
	public static PersistedCodeListItem fromItem(CodeListItem item) {
		PersistedCodeListItem result = new PersistedCodeListItem(item.getCodeList(), item.getId(), item.getLevel());
		result.setCode(item.getCode());
		result.setQualifiable(item.isQualifiable());
		result.setDeprecatedVersion(item.getDeprecatedVersion());
		result.setSinceVersion(item.getSinceVersion());
		List<LanguageSpecificText> descriptions = item.getDescriptions();
		for (LanguageSpecificText languageSpecificText : descriptions) {
			result.setDescription(languageSpecificText.getLanguage(), languageSpecificText.getText());
		}
		List<LanguageSpecificText> labels = item.getLabels();
		for (LanguageSpecificText languageSpecificText : labels) {
			result.setLabel(languageSpecificText.getLanguage(), languageSpecificText.getText());
		}
		Set<QName> annotationNames = item.getAnnotationNames();
		for (QName qName : annotationNames) {
			result.setAnnotation(qName, item.getAnnotation(qName));
		}
		return result;
	}

	public PersistedCodeListItem(CodeList codeList, int level) {
		this(codeList, codeList.getSurvey().nextId(), level);
	}

	public PersistedCodeListItem(CodeList codeList, int id, int level) {
		super(codeList, id, level);
	}

	public Integer getSystemId() {
		return systemId;
	}

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((parentId == null) ? 0 : parentId.hashCode());
		result = prime * result
				+ ((sortOrder == null) ? 0 : sortOrder.hashCode());
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
		PersistedCodeListItem other = (PersistedCodeListItem) obj;
		if (parentId == null) {
			if (other.parentId != null)
				return false;
		} else if (!parentId.equals(other.parentId))
			return false;
		if (sortOrder == null) {
			if (other.sortOrder != null)
				return false;
		} else if (!sortOrder.equals(other.sortOrder))
			return false;
		if (systemId == null) {
			if (other.systemId != null)
				return false;
		} else if (!systemId.equals(other.systemId))
			return false;
		return true;
	}

}