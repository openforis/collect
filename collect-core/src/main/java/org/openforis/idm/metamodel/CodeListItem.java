/**
 * 
 */
package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.collection.CollectionUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class CodeListItem extends VersionableSurveyObject implements Serializable {

	private static final long serialVersionUID = 1L;

	private Boolean qualifiable;
	private String code;
	private LanguageSpecificTextMap labels;
	private LanguageSpecificTextMap descriptions;
	private List<CodeListItem> childItems;
	private CodeList list;
	private CodeListItem parentItem;
	private final int level;
	private String imageFileName;
	private String color;

	protected CodeListItem(CodeList codeList, int id, int level) {
		super(codeList.getSurvey(), id);
		this.list = codeList;
		this.level = level;
	}

	public boolean hasChildItems() {
		return ! ( childItems == null || childItems.isEmpty() );
	}
	
	/**
	 * @deprecated Use getLevel instead
	 */
	@Deprecated
	public int getDepth() {
		int depth = 1;
		CodeListItem parent = parentItem;
		while ( parent != null ) {
			parent = parent.getParentItem();
			depth++;
		}
		return depth;
	}
	
	public int getLevel() {
		return level;
	}
	
	public boolean isQualifiable() {
		return qualifiable == null ? false : qualifiable;
	}
	
	public void setQualifiable(Boolean qualifiable) {
		this.qualifiable = qualifiable;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public List<LanguageSpecificText> getLabels() {
		if ( this.labels == null ) {
			return Collections.emptyList();
		} else {
			return this.labels.values();
		}
	}
	
	/**
	 * Returns the label in the default language
	 */
	public String getLabel() {
		return getLabel(null, true);
	}
	
	/**
	 * Returns the label in the specified language
	 * (if language is null, it uses the default survey language)
	 */
	public String getLabel(String language) {
		return getLabel(language, false);
	}
	
	/**
	 * Returns the label in the specified language.
	 * If not found, returns the one in the survey default language
	 */
	public String getLabel(String language, boolean defaultToSurveyDefaultLanguage) {
		if (labels == null) {
			return null;
		}
		String defaultLanguage = defaultToSurveyDefaultLanguage ? getSurvey().getDefaultLanguage(): null;
		return labels.getText(language, defaultLanguage);
	}
	
	public void addLabel(LanguageSpecificText label) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.add(label);
	}

	public void setLabel(String language, String text) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.setText(language, text);
	}
	
	public void removeLabel(String language) {
		if ( labels != null ) {
			labels.remove(language);
		}
	}
	
	public void removeAllLabels() {
		if ( labels != null ) {
			labels.removeAll();
		}
	}

	public List<LanguageSpecificText> getDescriptions() {
		if ( descriptions == null ) {
			return Collections.emptyList();
		} else {
			return descriptions.values();
		}
	}

	public String getDescription() {
		return getDescription(null, true);
	}
	
	public String getDescription(String language) {
		return getDescription(language, false);
	}
	
	/**
	 * Returns the description in the specified language.
	 * If not found, returns the one in the survey default language
	 */
	public String getDescription(String language, boolean defaultToSurveyDefaultLanguage) {
		if (descriptions == null) {
			return null;
		}
		String defaultLanguage = defaultToSurveyDefaultLanguage ? getSurvey().getDefaultLanguage(): null;
		return descriptions.getText(language, defaultLanguage);
	}
	
	public void setDescription(String language, String text) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.setText(language, text);
	}
	
	public void addDescription(LanguageSpecificText description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.add(description);
	}

	public void removeDescription(String language) {
		if ( descriptions != null ) {
			descriptions.remove(language);
		}
	}
	
	public void removeAllDescriptions() {
		if ( descriptions != null ) {
			descriptions.removeAll();
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> getChildItems() {
		return (List<T>) CollectionUtils.unmodifiableList(childItems);
	}
	
	public CodeListItem getChildItem(String code) {
		if ( childItems != null && code != null) {
			for (CodeListItem item : childItems) {
				if ( code.equals(item.getCode()) ) {
					return item;
				}
			}
		}
		return null;
	}
	
	public CodeListItem findChildItem(String code) {
		if ( childItems != null && code != null ) {
			Pattern pattern = createMatchingPattern(code);
			for (CodeListItem item : childItems) {
				if ( item.matchCode(pattern) ) {
					return item;
				}
			}
		}
		return null;
	}
	
	protected Pattern createMatchingPattern(String code) {
		String adaptedCode = Pattern.quote(code);
		Pattern pattern = Pattern.compile("^" + adaptedCode + "$", Pattern.CASE_INSENSITIVE);
		return pattern;
	}
	
	public boolean matchCode(String code) {
		Pattern pattern = createMatchingPattern(code);
		return matchCode(pattern);
	}

	protected boolean matchCode(Pattern pattern) {
		String itemCode = getCode();
		Matcher matcher = pattern.matcher(itemCode);
		if(matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public void addChildItem(CodeListItem item) {
		if ( childItems == null ) {
			childItems = new ArrayList<CodeListItem>();
		}
		// TODO check id is unique and don't exceed max
//		item.setId(nextItemId());
		childItems.add(item);
		item.setParentItem(this);
	}
	
	public void removeChildItem(int id) {
		if ( childItems != null ) {
			Iterator<CodeListItem> it = childItems.iterator();
			while ( it.hasNext() ) {
				CodeListItem item = it.next();
				if ( item.getId() == id ) {
					it.remove();
				}
			}
		}
	}

	public void moveChildItem(CodeListItem item, int indexTo) {
		CollectionUtils.shiftItem(childItems, item, indexTo);
	}

	protected int calculateLastUsedItemId() {
		int result = 0;
		List<CodeListItem> items = getChildItems();
		for (CodeListItem item : items) {
			result = Math.max(result, item.getId());	
		}
		return result;
	}
	
	public CodeListItem getParentItem() {
		return parentItem;
	}
	
	public void setParentItem(CodeListItem parentItem) {
		this.parentItem = parentItem;
	}
	
	public CodeList getCodeList() {
		return list;
	}

	boolean isQualifiableRecursive() {
		if ( isQualifiable() ) {
			return true;
		}
		for (CodeListItem child : getChildItems()) {
			if ( child.isQualifiableRecursive() ) {
				return true;
			}
		}
		return false;
	}
	
	public void removeVersioningRecursive(ModelVersion version) {
		removeVersioning(version);
		if ( childItems != null ) {
			for (CodeListItem child : childItems ) {
				child.removeVersioningRecursive(version);
			}
		}
	}
	
	public boolean hasUploadedImage() {
		return StringUtils.isNotBlank(imageFileName);
	}
	
	public String getImageFileName() {
		return imageFileName;
	}
	
	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(getId());
		sb.append(']');
		if (code != null) {
			sb.append(' ');
			sb.append(code);
		}
		String label = getLabel();
		if (label != null) {
			sb.append(' ');
			sb.append('(');
			sb.append(label);
			sb.append(')');
		}
		return sb.toString();
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeListItem other = (CodeListItem) obj;
		if (childItems == null) {
			if (other.childItems != null)
				return false;
		} else if (!childItems.equals(other.childItems))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		} else if (!descriptions.equals(other.descriptions))
			return false;
		if (getId() != other.getId())
			return false;
		if (imageFileName == null) {
			if (other.imageFileName != null)
				return false;
		} else if (!imageFileName.equals(other.imageFileName))
			return false;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (qualifiable == null) {
			if (other.qualifiable != null)
				return false;
		} else if (!qualifiable.equals(other.qualifiable))
			return false;
		return true;
	}

}
