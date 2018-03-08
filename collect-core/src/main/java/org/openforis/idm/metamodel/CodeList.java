package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Visitor;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CodeList extends VersionableSurveyObject {

	private static final long serialVersionUID = 1L;

	public enum CodeScope {
		SCHEME, LOCAL
	}

	private String name;
	private String lookupTable;
	private CodeListLabelMap labels;
	private LanguageSpecificTextMap descriptions;
	private CodingScheme codingScheme;
	private List<CodeListLevel> hierarchy;
	private List<CodeListItem> items;

	CodeList(Survey survey, int id) {
		super(survey, id);
	}

	public String getName() {
		return this.name;
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

	public List<CodeListLabel> getLabels() {
		if ( labels == null ) {
			return Collections.emptyList();
		} else {
			return labels.values();
		}
	}
	
	public String getLabel(CodeListLabel.Type type) {
		return getLabel(type, getSurvey().getDefaultLanguage());
	}
	
	public String getLabel(CodeListLabel.Type type, String language) {
		return labels == null ? null: labels.getText(type, language);
	}
	
	public void setLabel(CodeListLabel.Type type, String language, String text) {
		if ( labels == null ) {
			labels = new CodeListLabelMap();
		}
		labels.setText(type, language, text);
	}

	public void addLabel(CodeListLabel label) {
		if ( labels == null ) {
			labels = new CodeListLabelMap();
		}
		labels.add(label);
	}

	public void removeLabel(CodeListLabel.Type type, String language) {
		if (labels != null ) {
			labels.remove(type, language);
		}
	}

	public List<LanguageSpecificText> getDescriptions() {
		if ( this.descriptions == null ) {
			return Collections.emptyList();
		} else {
			return this.descriptions.values();
		}
	}
	
	public String getDescription() {
		return getDescription(getSurvey().getDefaultLanguage());
	}

	public String getDescription(String language) {
		if ( language == null ) {
			language = getSurvey().getDefaultLanguage();
		}
		return descriptions == null ? null: descriptions.getText(language);
	}
	
	public void setDescription(String language, String description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.setText(language, description);
	}
	
	public void addDescription(LanguageSpecificText description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.add(description);
	}

	public void removeDescription(String language) {
		descriptions.remove(language);
	}

	public List<CodeListLevel> getHierarchy() {
		return CollectionUtils.unmodifiableList(this.hierarchy);
	}

	public boolean isExternal() {
		return StringUtils.isNotBlank(lookupTable);
	}
	
	public boolean isEmpty() {
		return getItems().isEmpty();
	}

	public boolean isHierarchical() {
		return hierarchy != null && hierarchy.size() > 1;
	}

	public void addLevel(CodeListLevel level) {
		if ( this.hierarchy == null ) {
			this.hierarchy = new ArrayList<CodeListLevel>();
		}
		this.hierarchy.add(level);
	}
	
	public void removeLevel(int index) {
		if ( index >= 0 ) {
			hierarchy.remove(index);
			if ( index > 0 ) {
				removeItemsInLevel(index);
			}
		}
	}
	
	/**
	 * Removes all levels in hierarchy and all the child items
	 * 
	 */
	public void removeAllLevels() {
		removeAllItems();
		hierarchy = null;
	}

	public boolean hasItemsInLevel(int levelIndex) {
		List<CodeListItem> itemsInLevel = getItemsInLevel(levelIndex);
		return ! itemsInLevel.isEmpty();
	}
	
	protected List<CodeListItem> getItemsInLevel(int levelIndex) {
		List<CodeListItem> itemsInLevel = getItems();
		for (int depth = 0; ! itemsInLevel.isEmpty() && depth < levelIndex; depth ++ ) {
			List<CodeListItem> itemsInNextLevel = new ArrayList<CodeListItem>();
			for (CodeListItem item : itemsInLevel) {
				List<CodeListItem> childItems = item.getChildItems();
				if ( ! childItems.isEmpty() ) {
					itemsInNextLevel.addAll(childItems);
				}
			}
			itemsInLevel = itemsInNextLevel;
		}
		return itemsInLevel;
	}
	
	protected void removeItemsInLevel(int levelIndex) {
		List<CodeListItem> itemsInLevel = getItemsInLevel(levelIndex);
		for (CodeListItem item : itemsInLevel) {
			CodeListItem parent = item.getParentItem();
			int itemId = item.getId();
			if ( parent != null ) {
				parent.removeChildItem(itemId);
			} else {
				this.removeItem(itemId);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> getItems() {
		if ( isExternal() ) {
			throw new UnsupportedOperationException();
		} else {
			return (List<T>) CollectionUtils.unmodifiableList(this.items);
		}
	}

	public List<CodeListItem> getItems(Integer levelIdx) {
		return getItems(levelIdx, null);
	}
	
	public List<CodeListItem> getItems(Integer levelIdx, ModelVersion version) {
		List<CodeListItem> items = getItemsInternal(this.items, levelIdx, version);
		return CollectionUtils.unmodifiableList(items);
	}

	private List<CodeListItem> getItemsInternal(List<CodeListItem> parentItems, Integer levelIdx,
			ModelVersion version) {
		if ( levelIdx == null || levelIdx <= 0 ) {
			return parentItems;
		} else {
			ArrayList<CodeListItem> descendants = new ArrayList<CodeListItem>();
			for (CodeListItem item : parentItems) {
				List<CodeListItem> childItems = item.getChildItems();
				for (CodeListItem childItem : childItems) {
					if ( version == null || version.isApplicable(childItem) ) {
						descendants.add(childItem);
					}
				}
			}
			return getItemsInternal(descendants, levelIdx - 1, version);
		}
	}

	public CodeListItem getItem(int itemId) {
		Deque<CodeListItem> stack = new LinkedList<CodeListItem>();
		stack.addAll(getItems());
		while (! stack.isEmpty()) {
			CodeListItem item = stack.pop();
			if (item.getId() == itemId) {
				return item;
			}
		}
		return null;
	}
	
	public CodeListItem getItem(String code) {
		return getItem(code, null);
	}
	
	public CodeListItem getItem(String code, Integer levelIdx) {
		return getItem(code, levelIdx, null);
	}
	
	public CodeListItem getItem(String code, Integer levelIdx, ModelVersion version) {
		List<CodeListItem> items = getItems(levelIdx, version);
		for (CodeListItem item : items) {
			if ( code.equals(item.getCode()) ) {
				return item;
			}
		}
		return null;
	}
	
	public CodeListItem createItem(int id, int level) {
		return new CodeListItem(this, id, level);
	}

	public CodeListItem createItem(int level) {
		int id = getSurvey().nextId();
		return createItem(id, level);
	}

	@Deprecated
	public CodeListItem findItem(String code) {
		if ( items != null && code != null ) {
			String adaptedCode = Pattern.quote(code);
			Pattern pattern = Pattern.compile("^" + adaptedCode + "$", Pattern.CASE_INSENSITIVE);
			for (CodeListItem item : items) {
				String itemCode = item.getCode();
				Matcher matcher = pattern.matcher(itemCode);
				if(matcher.find()) {
					return item;
				}
			}
		}
		return null;
	}

	public void addItem(CodeListItem item) {
		if ( items == null ) {
			items = new ArrayList<CodeListItem>();
		}
		// TODO check id
		items.add(item);
	}
	
	/**
	 * 
	 * Removes all child items
	 * 
	 */
	public void removeAllItems() {
		if (items != null) {
			items.clear();
		}
		items = null;
	}
	
	public void removeItem(int id) {
		if ( items != null ) {
			Iterator<CodeListItem> it = items.iterator();
			while ( it.hasNext() ) {
				CodeListItem item = it.next();
				if ( item.getId() == id ) {
					it.remove();
					break;
				}
			}
		}
	}
	
	public void moveItem(CodeListItem item, int indexTo) {
		CollectionUtils.shiftItem(items, item, indexTo);
	}
	
	public void visitItems(Visitor<CodeListItem> visitor, ModelVersion version) {
		Deque<CodeListItem> stack = new LinkedList<CodeListItem>();
		stack.addAll(getItems());
		while (! stack.isEmpty()) {
			CodeListItem item = stack.pop();
			if (version == null || version.isApplicable(item)) {
				visitor.visit(item);
				stack.addAll(item.getChildItems());
			}
		}
	}

	public CodeScope getCodeScope() {
		if ( codingScheme == null || codingScheme.getCodeScope() == null ) {
			return null;
		} else {
			return codingScheme.getCodeScope();
		}
	}

	public void setCodeScope(CodeScope scope) {
		if ( codingScheme == null ) {
			this.codingScheme = new CodingScheme();
		}
		codingScheme.setCodeScope(scope); 
	}
	
	public boolean isQualifiable() {
		for (CodeListItem item : getItems()) {
			if ( item.isQualifiableRecursive() ) {
				return true;
			}
		}
		return false;
	}
	
	public void removeVersioningRecursive(ModelVersion version) {
		removeVersioning(version);
		if ( ! isExternal() ) {
			for (CodeListItem item : getItems()) {
				item.removeVersioningRecursive(version);
			}
		}
	}
	
	public boolean isEnumeratingList() {
		Survey survey = getSurvey();
		Schema schema = survey.getSchema();
		Deque<NodeDefinition> stack = new LinkedList<NodeDefinition>();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		stack.addAll(rootEntityDefinitions);
		while ( ! stack.isEmpty() ) {
			NodeDefinition node = stack.pop();
			if ( node instanceof EntityDefinition ) {
				EntityDefinition entityDefn = (EntityDefinition) node;
				if ( ! entityDefn.isRoot() ) {
					CodeAttributeDefinition enumeratingKeyCodeAttribute = entityDefn.getEnumeratingKeyCodeAttribute();
					if ( enumeratingKeyCodeAttribute != null && entityDefn.isEnumerate() && 
							enumeratingKeyCodeAttribute.getList().getId() == this.getId() ) {
						return true;
					}
				}
				stack.addAll(entityDefn.getChildDefinitions());
			}
		}
		return false;
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeList other = (CodeList) obj;
		if (codingScheme == null) {
			if (other.codingScheme != null)
				return false;
		} else if (!codingScheme.equals(other.codingScheme))
			return false;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		} else if (!descriptions.equals(other.descriptions))
			return false;
		if (hierarchy == null) {
			if (other.hierarchy != null)
				return false;
		} else if (!hierarchy.equals(other.hierarchy))
			return false;
		if (getId() != other.getId())
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (lookupTable == null) {
			if (other.lookupTable != null)
				return false;
		} else if (!lookupTable.equals(other.lookupTable))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
