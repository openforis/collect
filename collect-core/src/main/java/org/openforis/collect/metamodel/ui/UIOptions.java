package org.openforis.collect.metamodel.ui;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_TYPE;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.CODE_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVerifier;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TextAttributeDefinition;


/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UIOptions implements ApplicationOptions, Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String VISIBLE_FIELDS_SEPARATOR = ",";
	
	private static final String TABSET_NAME_PREFIX = "tabset_";
	private static final String TAB_NAME_PREFIX = "tab_";

	public static final List<String[]> TAXON_VISIBLE_FIELDS_TEMPLATES = Arrays.asList(
		new String[] {
			CODE_FIELD_NAME,
			SCIENTIFIC_NAME_FIELD_NAME,
			VERNACULAR_NAME_FIELD_NAME
		},
		new String[] {
			CODE_FIELD_NAME,
			SCIENTIFIC_NAME_FIELD_NAME
		},
		new String[] {
			CODE_FIELD_NAME,
			VERNACULAR_NAME_FIELD_NAME
		},
		new String[] {
			SCIENTIFIC_NAME_FIELD_NAME,
			VERNACULAR_NAME_FIELD_NAME
		},
		new String[] {
			VERNACULAR_NAME_FIELD_NAME
		}
	);
	
	public enum CoordinateAttributeFieldsOrder {
		
		SRS_X_Y("srs_x_y"), 
		SRS_Y_X("srs_y_x"),
		X_Y_SRS("x_y_srs"), 
		Y_X_SRS("y_x_srs");
		
		public static final CoordinateAttributeFieldsOrder DEFAULT = SRS_Y_X;
		
		private String value;

		private CoordinateAttributeFieldsOrder(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
	}
	
	public enum Layout {
		FORM, TABLE
	}
	
	public enum CodeAttributeLayoutType {
		RADIO, DROPDOWN, TEXT;
		
		@Override
		public String toString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
		
	}
	
	public enum Direction {
		BY_ROWS("byRows"), 
		BY_COLUMNS("byColumns");
		
		private String value;

		private Direction(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public enum Orientation {
		HORIZONTAL, VERTICAL
	}
	
	private CollectSurvey survey;
	private List<UITabSet> tabSets;
	
	public UIOptions(CollectSurvey survey) {
		this.survey = survey;
	}

	@Override
	public String getType() {
		return UI_TYPE;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public List<UITabSet> getTabSets() {
		return CollectionUtils.unmodifiableList(tabSets);
	}
	
	public UITabSet createTabSet() {
		return createTabSet(TABSET_NAME_PREFIX + survey.nextId());
	}
	
	public UITabSet createTabSet(String name) {
		UITabSet result = new UITabSet(this);
		result.setName(name);
		return result;
	}
	
	public UITab createTab() {
		return createTab(TAB_NAME_PREFIX + survey.nextId());
	}

	public UITab createTab(String name) {
		UITab result = new UITab(this);
		result.setName(name);
		return result;
	}
	
	public UITabSet createRootTabSet(EntityDefinition rootEntity) {
		UIOptions uiOpts = survey.getUIOptions();
		UITabSet tabSet = uiOpts.createTabSet();
		UITab mainTab = createMainTab(rootEntity, tabSet);
		uiOpts.addTabSet(tabSet);
		uiOpts.assignToTabSet(rootEntity, tabSet);
		uiOpts.assignToTab(rootEntity, mainTab);
		return tabSet;
	}

	protected UITab createMainTab(EntityDefinition nodeDefn,
			UITabSet tabSet) {
		UITab tab = createTab();
		copyLabels(nodeDefn, tab);
		tabSet.addTab(tab);
		return tab;
	}

	protected void copyLabels(EntityDefinition nodeDefn, UITab tab) {
		removeLabels(tab);
		List<NodeLabel> labels = nodeDefn.getLabels();
		for (NodeLabel label : labels) {
			if ( label.getType() == NodeLabel.Type.INSTANCE ) {
				tab.setLabel(label.getLanguage(), label.getText());
			}
		}
	}

	protected void removeLabels(UITab tab) {
		List<LanguageSpecificText> labels = tab.getLabels();
		for (LanguageSpecificText label : labels) {
			tab.removeLabel(label.getLanguage());
		}
	}
	
	public UITab getMainTab(UITabSet rootTabSet) {
		List<UITab> tabs = rootTabSet.getTabs();
		if ( tabs.isEmpty() ) {
			return null;
		} else {
			return tabs.get(0);
		}
	}
	
	public boolean isMainTab(UITab tab) {
		return tab.getIndex() == 0 && tab.getDepth() == 1;
	}
	
	public UITab getTab(String name) {
		Deque<UITab> stack = new LinkedList<UITab>();
		List<UITabSet> tabSets = getTabSets();
		for (UITabSet tabSet : tabSets) {
			stack.addAll(tabSet.getTabs());
			while ( ! stack.isEmpty() ) {
				UITab tab = stack.pop();
				if ( name.equals(tab.getName()) ) {
					return tab;
				}
				stack.addAll(tab.getTabs());
			}
		}
		return null;
	}
	
	public UITab getAssignedTab(NodeDefinition nodeDefn) {
		return getAssignedTab(nodeDefn, true);
	}
	
	public UITab getAssignedTab(NodeDefinition nodeDefn, boolean includeInherited) {
		EntityDefinition parentDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		return getAssignedTab(parentDefn, nodeDefn, includeInherited);
	}
	
	public UITab getAssignedTab(EntityDefinition parentDefn, NodeDefinition nodeDefn, boolean inheritIfNull) {
		UITabSet rootTabSet = getAssignedRootTabSet(parentDefn, nodeDefn);
		if ( rootTabSet == null ) {
			return null;
		}
		String tabName = nodeDefn.getAnnotation(Annotation.TAB_NAME.getQName());
		if ( StringUtils.isBlank(tabName) ) {
			if ( inheritIfNull ) {
				return getAssignedTab(parentDefn);
			} else {
				return null;
			}
		} else if ( parentDefn == null || parentDefn.isRoot() ) {
			UITab result = null;
			result = rootTabSet.getTab(tabName);
			if ( result == null ) {
				//try to find tab among main tab children
				UITab mainTab = getMainTab(rootTabSet);
				result = mainTab == null ? null : mainTab.getTab(tabName);
			}
			return result;
		} else {
			UITab parentTab = getAssignedTab(parentDefn);
			if ( parentTab == null ) {
				return null;
			} else if (! parentDefn.isMultiple() || tabName.equals(parentTab.getName()) ) {
				//single entities cannot have nested tabs, ignore them when found
				return parentTab;
			} else {
				return parentTab.getTab(tabName);
			}
		}
	}

	protected UITabSet getAssignedRootTabSet(EntityDefinition parentDefn, NodeDefinition nodeDefn) {
		EntityDefinition rootEntityDefn;
		if ( parentDefn != null ) {
			rootEntityDefn = parentDefn.getRootEntity();
		} else if ( nodeDefn instanceof EntityDefinition ) {
			rootEntityDefn = (EntityDefinition) nodeDefn;
		} else {
			throw new IllegalArgumentException("Parent entity definition not specified");
		}
		UITabSet tabSet = getAssignedRootTabSet(rootEntityDefn);
		return tabSet;
	}

	public UITabSet getAssignedRootTabSet(EntityDefinition rootEntityDefn) {
		String tabSetName = rootEntityDefn.getAnnotation(Annotation.TAB_SET.getQName());
		UITabSet tabSet = getTabSet(tabSetName);
		return tabSet;
	}
	
	public List<UITab> getAssignableTabs(EntityDefinition parentEntity, NodeDefinition contextNode) {
		if (parentEntity.isMultiple() || parentEntity.isRoot()) {
			boolean contextNodeIsFormLayoutMultipleEntity = contextNode instanceof EntityDefinition && contextNode.isMultiple() && 
					getLayout((EntityDefinition) contextNode) == Layout.FORM;
			List<UITab> result = new ArrayList<UITab>(getTabsAssignableToChildren(parentEntity));
			Iterator<UITab> it = result.iterator();
			while (it.hasNext()) {
				UITab tab = (UITab) it.next();
				boolean mainTab = isMainTab(tab);
				if ( mainTab && contextNodeIsFormLayoutMultipleEntity ) {
					it.remove();
				}
			}
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	public UITabSet getAssignedTabSet(EntityDefinition entityDefn) {
		if ( entityDefn.getParentDefinition() == null ) {
			return getAssignedRootTabSet(entityDefn);
		} else {
			return getAssignedTab(entityDefn);
		}
	}

	public List<UITab> getTabsAssignableToChildren(EntityDefinition entityDefn) {
		return getTabsAssignableToChildren(entityDefn, true);
	}
	
	public List<UITab> getTabsAssignableToChildren(EntityDefinition entityDefn, boolean includeAlreadyAssignedTabs) {
		EntityDefinition rootEntity = entityDefn.getRootEntity();
		UITabSet rootTabSet = getAssignedRootTabSet(rootEntity);
		if ( rootTabSet == null || getLayout(entityDefn) == Layout.TABLE) {
			return Collections.emptyList();
		} else if ( entityDefn == rootEntity ) {
			List<UITab> tabs = new ArrayList<UITab>(rootTabSet.getTabs());
			UITab mainTab = getMainTab(rootTabSet);
			tabs.addAll(mainTab.getTabs());
			return tabs;
		} else {
			UITab assignedTab = getAssignedTab(entityDefn);
			List<UITab> childTabs = assignedTab.getTabs();
			List<UITab> validTabs;
			if ( includeAlreadyAssignedTabs ) {
				validTabs = childTabs;
			} else {
				validTabs = new ArrayList<UITab>();
				for (UITab childTab : childTabs) {
					boolean valid = true;
					List<NodeDefinition> nodesPerTab = getNodesPerTab(childTab, false);
					for (NodeDefinition node : nodesPerTab) {
						if ( ! node.isDescendantOf(entityDefn) ) {
							valid = false;
							break;
						}
					}
					if ( valid ) {
						validTabs.add(childTab);
					}
				}
			}
			return validTabs;
		}
	}
	
	public boolean isAssignableTo(NodeDefinition nodeDefn, UITab tab) {
		EntityDefinition parentEntityDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		List<UITab> allowedTabs = getTabsAssignableToChildren(parentEntityDefn);
		boolean result = allowedTabs.contains(tab);
		return result;
	}
	
	/**
	 * Returns true if the specified tab is not assigned to one of the descendants of the belonging rootEntity
	 */
	public boolean isUnassigned(UITab tab) {
		UITabSet rootTabSet = tab.getRootTabSet();
		EntityDefinition rootEntityDefinition = getRootEntityDefinition(rootTabSet);
		return isUnassigned(tab, rootEntityDefinition);
	}
	/**
	 * Returns true if the specified tab is not assigned to one of the descendants of the specified rootEntity
	 */
	public boolean isUnassigned(UITab tab, EntityDefinition rootEntity) {
		Deque<NodeDefinition> stack = new LinkedList<NodeDefinition>();
		stack.add(rootEntity);
		while ( ! stack.isEmpty() ) {
			NodeDefinition childDefn = stack.pop();
			UITab assignedTab = getAssignedTab(childDefn, false);
			if ( tab == assignedTab ) {
				return false;
			}
			if ( childDefn instanceof EntityDefinition ) {
				List<NodeDefinition> nestedChildDefns = ((EntityDefinition) childDefn).getChildDefinitions();
				for (NodeDefinition nestedChildDefn : nestedChildDefns) {
					stack.push(nestedChildDefn);
				}
			}
		}
		return true;
	}
	
	public void removeUnassignedTabs() {
		List<UITabSet> rootTabSets = getTabSets();
		for (UITabSet rootTabSet : rootTabSets) {
			Deque<UITabSet> stack = new LinkedList<UITabSet>();
			stack.push(rootTabSet);
			while ( ! stack.isEmpty() ) {
				UITabSet tabSet = stack.pop();
				List<UITab> childTabs = tabSet.getTabs();
				if ( childTabs.isEmpty() ) {
					//leaf, remove it and all ancestors if leaf and unassigned
					if ( tabSet instanceof UITab ) {
						UITab leaf = (UITab) tabSet;
						while ( leaf.getParent() != null && isUnassigned(leaf) ) {
							UITabSet parent = leaf.getParent();
							parent.removeTab(leaf);
							if ( parent instanceof UITab && parent.getTabs().isEmpty() ) {
								leaf = (UITab) parent;
							} else {
								break;
							}
						}
					}
				} else {
					for (UITab childTab : tabSet.getTabs()) {
						stack.push(childTab);
					}
				}
			}
		}
	}

	public EntityDefinition getParentEntityForAssignedNodes(final UITab tab) {
		UITabSet root = tab.getRootTabSet();
		EntityDefinition rootEntity = getRootEntityDefinition(root);
		if (tab.getDepth() == 1) {
			return rootEntity;
		}
		EntityDefinition tabEntityDefn = rootEntity.getSchema().findNodeDefinition(new NodeDefinitionVerifier() {
			public boolean verify(NodeDefinition nodeDefn) {
				if ( nodeDefn instanceof EntityDefinition ) {
					EntityDefinition entityDefn = (EntityDefinition) nodeDefn;
					UITab assignedTab = getAssignedTab(entityDefn, false);
					return assignedTab == tab;
				} else {
					return false;
				}
			}
		});
		if (tabEntityDefn != null) {
			return tabEntityDefn;
		} else {
			UITabSet parentTab = tab.getParent();
			if (parentTab != null && parentTab instanceof UITab) {
				return getParentEntityForAssignedNodes((UITab) parentTab);
			} else {
				throw new IllegalStateException("Parent entity for assigned nodes not found for tab: " + tab.getName());
			}
		}
	}
	
	public boolean isAssociatedWithMultipleEntityForm(UITab tab) {
		return getFormLayoutMultipleEntity(tab) != null;
	}
	
	public List<NodeDefinition> getNodesPerTab(UITab tab, boolean includeDescendants) {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		UITabSet tabSet = tab.getRootTabSet();
		Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>();
		EntityDefinition rootEntity = getRootEntityDefinition(tabSet);
		queue.addAll(rootEntity.getChildDefinitions());
		while ( ! queue.isEmpty() ) {
			NodeDefinition defn = queue.remove();
			UITab nodeTab = getAssignedTab(defn);
			boolean nodeInTab = false;
			if ( nodeTab != null && nodeTab.equals(tab) ) {
				result.add(defn);
				nodeInTab = true;
			}
			if ( defn instanceof EntityDefinition && (includeDescendants || !nodeInTab) ) {
				queue.addAll(((EntityDefinition) defn).getChildDefinitions());
			}
		}
		return result;
	}

	public void assignToTabSet(EntityDefinition rootEntity, UITabSet tabSet) {
		String name = tabSet.getName();
		rootEntity.setAnnotation(Annotation.TAB_SET.getQName(), name);
	}

	public void assignToTab(NodeDefinition nodeDefn, UITab tab) {
		String tabName = tab.getName();
		nodeDefn.setAnnotation(Annotation.TAB_NAME.getQName(), tabName);
		
		afterTabAssociationChanged(nodeDefn);
	}

	protected void afterTabAssociationChanged(NodeDefinition nodeDefn) {
		if ( nodeDefn instanceof EntityDefinition && nodeDefn.getParentDefinition() != null ) {
			removeInvalidTabAssociationInDescendants((EntityDefinition) nodeDefn);
		}
	}

	protected void removeInvalidTabAssociationInDescendants(EntityDefinition entityDefn) {
		entityDefn.traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition descendantDefn) {
				UITab descendantTab = getAssignedTab(descendantDefn, false);
				if ( descendantTab == null ) {
					//tab not set or not found
					performTabAssociationRemoval(descendantDefn);
				}
			}
		});
	}
	
	public void removeTabAssociation(NodeDefinition nodeDefn) {
		performTabAssociationRemoval(nodeDefn);
		
		afterTabAssociationChanged(nodeDefn);
	}

	protected void performTabAssociationRemoval(NodeDefinition nodeDefn) {
		nodeDefn.setAnnotation(Annotation.TAB_NAME.getQName(), null);
	}
	
	public void removeTabAssociation(UITab tab) {
		Deque<UITab> stack = new LinkedList<UITab>();
		stack.push(tab);
		while ( ! stack.isEmpty() ) {
			UITab currentTab = stack.pop();
			List<NodeDefinition> nodesPerTab = getNodesPerTab(currentTab, true);
			for (NodeDefinition nodeDefn : nodesPerTab) {
				performTabAssociationRemoval(nodeDefn);
			}
			List<UITab> childTabs = currentTab.getTabs();
			stack.addAll(childTabs);
		}
	}

	public boolean isAssociatedToTab(NodeDefinition nodeDefn) {
		UITab tab = getAssignedTab(nodeDefn);
		return tab != null;
	}
	
	public Layout getLayout(EntityDefinition node) {
		String layoutValue = node.getAnnotation(Annotation.LAYOUT.getQName());
		if ( layoutValue == null ) {
			EntityDefinition parent = (EntityDefinition) node.getParentDefinition();
			if ( parent != null ) {
				if ( node.isMultiple()) {
					return Layout.TABLE;
				} else {
					return getLayout(parent);
				}
			} else {
				return Layout.FORM;
			}
		}
		return layoutValue == null ? null : Layout.valueOf(layoutValue.toUpperCase());
	}

	public void setLayout(EntityDefinition entityDefn, Layout layout) {
		String layoutValue = layout != null ? layout.name().toLowerCase(Locale.ENGLISH): null;
		entityDefn.setAnnotation(Annotation.LAYOUT.getQName(), layoutValue);
	}
	
	public Direction getDirection(EntityDefinition defn) {
		String value = defn.getAnnotation(Annotation.DIRECTION.getQName());
		if ( value == null ) {
			EntityDefinition parentDefn = (EntityDefinition) defn.getParentDefinition();
			if ( parentDefn == null ) {
				return Direction.BY_ROWS;
			} else {
				return getDirection(parentDefn);
			}
		} else if ( value.equals(Direction.BY_COLUMNS.getValue())) {
			return Direction.BY_COLUMNS;
		} else {
			return Direction.BY_ROWS;
		}
	}
	
	public void setDirection(EntityDefinition defn, Direction direction) {
		String value = direction == null ? null: direction.getValue();
		defn.setAnnotation(Annotation.DIRECTION.getQName(), value);
	}
	
	public CoordinateAttributeFieldsOrder getFieldsOrder(CoordinateAttributeDefinition defn) {
		String value = defn.getAnnotation(Annotation.FIELDS_ORDER.getQName());
		if ( value == null ) {
			return CoordinateAttributeFieldsOrder.DEFAULT;
		} else {
			return CoordinateAttributeFieldsOrder.valueOf(value.toUpperCase());
		}
	}
	
	public void setFieldsOrder(CoordinateAttributeDefinition defn, CoordinateAttributeFieldsOrder fieldsOrder) {
		String value;
		if ( fieldsOrder == null || fieldsOrder == CoordinateAttributeFieldsOrder.DEFAULT ) {
			value = null;
		} else {
			value = fieldsOrder.name().toLowerCase(Locale.ENGLISH);
		}
		defn.setAnnotation(Annotation.FIELDS_ORDER.getQName(), value);
	}
	
	public boolean isVisibleField(AttributeDefinition defn, String field) {
		String[] visibleFields = getVisibleFields(defn);
		return Arrays.asList(visibleFields).contains(field);
	}
	
	public String[] getVisibleFields(AttributeDefinition defn) {
		String value = defn.getAnnotation(Annotation.VISIBLE_FIELDS.getQName());
		if ( StringUtils.isBlank(value) ) {
			//when no annotation is specified, all fields are visible
			List<String> fieldNames = defn.getFieldNames();
			return fieldNames.toArray(new String[fieldNames.size()]);
		} else {
			String[] fields = value.split(VISIBLE_FIELDS_SEPARATOR);
			return fields;
		}
	}
	
	public void setVisibleFields(AttributeDefinition defn, String[] fields) {
		String value;
		if ( fields == null || fields.length == defn.getFieldNames().size() ) {
			//do not store annotation value when all fields are visible
			value = null;
		} else {
			value = StringUtils.join(fields, VISIBLE_FIELDS_SEPARATOR);
		}
		defn.setAnnotation(Annotation.VISIBLE_FIELDS.getQName(), value);
	}
	
	public boolean getShowRowNumbersValue(EntityDefinition defn) {
		String annotationValue = defn.getAnnotation(Annotation.SHOW_ROW_NUMBERS.getQName());
		return Boolean.valueOf(annotationValue);
	}
	
	public void setShowRowNumbersValue(EntityDefinition defn, boolean value) {
		defn.setAnnotation(Annotation.SHOW_ROW_NUMBERS.getQName(), value ? Boolean.toString(value): null);
	}
	
	public boolean getCountInSumamryListValue(EntityDefinition defn) {
		String annotationValue = defn.getAnnotation(Annotation.COUNT_IN_SUMMARY_LIST.getQName());
		return Boolean.valueOf(annotationValue);
	}
	
	public void setCountInSummaryListValue(EntityDefinition defn, boolean value) {
		defn.setAnnotation(Annotation.COUNT_IN_SUMMARY_LIST.getQName(), value ? Boolean.toString(value): null);
	}
	
	public CodeAttributeLayoutType getLayoutType(CodeAttributeDefinition defn) {
		CodeAttributeLayoutType value = getAnnotationEnumValue(defn, Annotation.CODE_ATTRIBUTE_LAYOUT_TYPE, CodeAttributeLayoutType.class);
		return value;
	}
	
	public void setLayoutType(CodeAttributeDefinition defn, CodeAttributeLayoutType value) {
		setAnnotationValue(defn, Annotation.CODE_ATTRIBUTE_LAYOUT_TYPE, value);
	}
	
	public Orientation getLayoutDirection(CodeAttributeDefinition defn) {
		return getAnnotationEnumValue(defn, Annotation.CODE_ATTRIBUTE_LAYOUT_DIRECTION, Orientation.class);
	}
	
	public void setLayoutDirection(CodeAttributeDefinition defn, Orientation value) {
		setAnnotationValue(defn, Annotation.CODE_ATTRIBUTE_LAYOUT_DIRECTION, value);
	}
	
	public boolean getShowAllowedValuesPreviewValue(CodeAttributeDefinition defn) {
		return getAnnotationBooleanValue(defn, Annotation.SHOW_ALLOWED_VALUES_PREVIEW);
	}
	
	public void setShowAllowedValuesPreviewValue(CodeAttributeDefinition defn, boolean value) {
		defn.setAnnotation(Annotation.SHOW_ALLOWED_VALUES_PREVIEW.getQName(), value ? Boolean.toString(value): null);
	}
	
	public boolean getShowCode(CodeAttributeDefinition defn) {
		return getAnnotationBooleanValue(defn, Annotation.CODE_ATTRIBUTE_SHOW_CODE);
	}
	
	public void setShowCode(CodeAttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.CODE_ATTRIBUTE_SHOW_CODE, value);
	}

	public boolean isHidden(NodeDefinition defn) {
		return getAnnotationBooleanValue(defn, Annotation.HIDE);
	}
	
	public void setHidden(NodeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.HIDE, value);
	}

	public boolean isHideWhenNotRelevant(NodeDefinition defn) {
		return getAnnotationBooleanValue(defn, Annotation.HIDE_WHEN_NOT_RELEVANT);
	}
	
	public void setHideWhenNotRelevant(NodeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.HIDE_WHEN_NOT_RELEVANT, value);
	}
	
	public int getColumn(NodeDefinition defn) {
		return getAnnotationIntegerValue(defn, Annotation.COLUMN);
	}
	
	public void setColumn(NodeDefinition defn, int value) {
		setAnnotationValue(defn, Annotation.COLUMN, value);
	}

	public int getColumnSpan(NodeDefinition defn) {
		return getAnnotationIntegerValue(defn, Annotation.COLUMN_SPAN);
	}
	
	public void setColumnSpan(NodeDefinition defn, int value) {
		setAnnotationValue(defn, Annotation.COLUMN_SPAN, value);
	}
	
	public Integer getWidth(NodeDefinition defn) {
		return getAnnotationIntegerValue(defn, Annotation.WIDTH);
	}
	
	public void setWidth(NodeDefinition defn, Integer value) {
		setAnnotationValue(defn, Annotation.WIDTH, value);
	}
	
	public Integer getLabelWidth(NodeDefinition defn) {
		return getAnnotationIntegerValue(defn, Annotation.LABEL_WIDTH);
	}
	
	public void setLabelWidth(NodeDefinition defn, Integer value) {
		setAnnotationValue(defn, Annotation.LABEL_WIDTH, value);
	}
	
	public Orientation getLabelOrientation(NodeDefinition defn) {
		return getAnnotationEnumValue(defn, Annotation.LABEL_ORIENTATION, Orientation.class);
	}
	
	public void setLabelOrientation(NodeDefinition defn, Orientation value) {
		setAnnotationValue(defn, Annotation.LABEL_ORIENTATION, value);
	}
	
	public boolean isAutoUppercase(TextAttributeDefinition defn) {
		return getAnnotationBooleanValue(defn, Annotation.AUTO_UPPERCASE);
	}
	
	public void setAutoUppercase(TextAttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.AUTO_UPPERCASE, value);
	}

	private boolean getAnnotationBooleanValue(NodeDefinition defn, Annotation annotation) {
		String annotationValue = defn.getAnnotation(annotation.getQName());
		if ( StringUtils.isBlank(annotationValue) ) {
			if( annotation.getDefaultValue() == null ) {
				return false;
			} else {
				return annotation.<Boolean>getDefaultValue().booleanValue();
			}
		} else {
			return Boolean.valueOf(annotationValue);
		}
	}

	private String getAnnotationStringValue(NodeDefinition defn, Annotation annotation) {
		String annotationValue = defn.getAnnotation(annotation.getQName());
		if ( StringUtils.isBlank(annotationValue) ) {
			if ( annotation.getDefaultValue() == null ) {
				return null;
			} else {
				return annotation.getDefaultValue();
			}
		} else {
			return annotationValue;
		}
	}
	
	private Integer getAnnotationIntegerValue(NodeDefinition defn, Annotation annotation) {
		String annotationValue = defn.getAnnotation(annotation.getQName());
		if ( StringUtils.isBlank(annotationValue) ) {
			if ( annotation.getDefaultValue() == null ) {
				return null;
			} else {
				Integer defaultValue = annotation.getDefaultValue();
				return defaultValue;
			}
		} else {
			return Integer.valueOf(annotationValue);
		}
	}
	
	private <E extends Enum<E>> E getAnnotationEnumValue(NodeDefinition defn, Annotation annotation, Class<E> enumType) {
		String annotationValue = defn.getAnnotation(annotation.getQName());
		if ( StringUtils.isBlank(annotationValue) ) {
			if ( annotation.getDefaultValue() == null ) {
				return null;
			} else {
				return annotation.<E>getDefaultValue();
			}
		} else {
			return Enum.valueOf(enumType, annotationValue.toUpperCase());
		}
	}
	
	private void setAnnotationValue(NodeDefinition defn, Annotation annotation, Object value) {
		String annotationValue;
		if ( value == null || annotation.getDefaultValue() != null && annotation.getDefaultValue().equals(value) ) {
			annotationValue = null;
		} else {
			annotationValue = value.toString();
		}
		defn.setAnnotation(annotation.getQName(), annotationValue);
	}
	
	/**
	 * Supported layouts are:
	 * - Root entity: FORM
	 * - Single entity: FORM and TABLE (only inside an entity with TABLE layout)
	 * - Multiple entity: FORM only if it's the only multiple entity with FORM layout
	 * 		into the tab, TABLE only if there is not an ancestor entity with TABLE layout
	 * 
	 * @param parentEntityDefn
	 * @param entityDefn
	 * @param layout
	 * @return 
	 */
	public boolean isLayoutSupported(EntityDefinition parentEntityDefn, int entityDefnId, UITab associatedTab, boolean multiple, Layout layout) {
		if ( parentEntityDefn == null ) {
			return layout == Layout.FORM;
		} else if ( ! multiple ) {
			Layout parentLayout = getLayout(parentEntityDefn);
			return layout == Layout.FORM || parentLayout == Layout.TABLE;
		} else if ( layout == Layout.FORM) {
			UITab tab = associatedTab == null ? getAssignedTab(parentEntityDefn, true): associatedTab;
			EntityDefinition multipleEntity = getFormLayoutMultipleEntity(tab);
			return multipleEntity == null || multipleEntity.getId() == entityDefnId;
		} else {
			EntityDefinition ancestorEntity = parentEntityDefn;
			while ( ancestorEntity != null ) {
				if ( getLayout(ancestorEntity) == Layout.TABLE) {
					return false;
				}
				ancestorEntity = (EntityDefinition) ancestorEntity.getParentDefinition();
			}
			return true;
		}
	}

	protected EntityDefinition getFormLayoutMultipleEntity(UITab tab) {
		List<NodeDefinition> nodesPerTab = getNodesPerTab(tab, false);
		for (NodeDefinition nodeDefn : nodesPerTab) {
			if ( nodeDefn instanceof EntityDefinition && nodeDefn.isMultiple() ) {
				Layout nodeLayout = getLayout((EntityDefinition) nodeDefn);
				if ( nodeLayout == Layout.FORM ) {
					return (EntityDefinition) nodeDefn;
				}
			}
		}
		return null;
	}

	public EntityDefinition getRootEntityDefinition(UITabSet tabSet) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition defn : rootEntityDefinitions) {
			UITabSet entityTabSet = getAssignedRootTabSet(defn);
			if ( entityTabSet != null && entityTabSet.equals(tabSet) ) {
				return defn;
			}
		}
		return null;
	}
	
	public UITabSet getTabSet(String name) {
		if ( tabSets != null ) {
			for (UITabSet tabSet : tabSets) {
				if ( tabSet.getName().equals(name) ) {
					return tabSet;
				}
			}
		}
		return null;
	}
	
	public void addTabSet(UITabSet tabSet) {
		if ( tabSets == null ) {
			tabSets = new ArrayList<UITabSet>();
		}
		tabSets.add(tabSet);
	}
	
	public void setTabSet(int index, UITabSet tabSet) {
		if ( tabSets == null ) {
			tabSets = new ArrayList<UITabSet>();
		}
		tabSets.set(index, tabSet);
	}
	
	public void removeTabSet(UITabSet tabSet) {
		tabSets.remove(tabSet);
	}
	
	public UITabSet updateTabSet(String name, String newName) {
		UITabSet tabSet = getTabSet(name);
		tabSet.setName(newName);
		return tabSet;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tabSets == null) ? 0 : tabSets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UIOptions other = (UIOptions) obj;
		if (tabSets == null) {
			if (other.tabSets != null)
				return false;
		} else if (!tabSets.equals(other.tabSets))
			return false;
		return true;
	}

}
