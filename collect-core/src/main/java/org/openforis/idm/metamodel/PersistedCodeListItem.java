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

	private Long systemId;
	private Long parentId;
	private Integer sortOrder;
	
	public static PersistedCodeListItem fromItem(CodeListItem item) {
		PersistedCodeListItem result = new PersistedCodeListItem(item.getCodeList(), item.getId(), item.getLevel());
		result.copyProperties(item);
		return result;
	}
	
	public void copyProperties(CodeListItem item) {
		this.setCode(item.getCode());
		this.setQualifiable(item.isQualifiable());
		this.setDeprecatedVersion(item.getDeprecatedVersion());
		this.setSinceVersion(item.getSinceVersion());
		List<LanguageSpecificText> descriptions = item.getDescriptions();
		for (LanguageSpecificText languageSpecificText : descriptions) {
			this.setDescription(languageSpecificText.getLanguage(), languageSpecificText.getText());
		}
		List<LanguageSpecificText> labels = item.getLabels();
		for (LanguageSpecificText languageSpecificText : labels) {
			this.setLabel(languageSpecificText.getLanguage(), languageSpecificText.getText());
		}
		Set<QName> annotationNames = item.getAnnotationNames();
		for (QName qName : annotationNames) {
			this.setAnnotation(qName, item.getAnnotation(qName));
		}
	}

	public PersistedCodeListItem(CodeList codeList, int level) {
		this(codeList, codeList.getSurvey().nextId(), level);
	}

	public PersistedCodeListItem(CodeList codeList, int id, int level) {
		super(codeList, id, level);
	}

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
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