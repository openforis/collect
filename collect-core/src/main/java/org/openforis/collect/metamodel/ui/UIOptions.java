package org.openforis.collect.metamodel.ui;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.util.CollectionUtil;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.*;


/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UIOptions implements ApplicationOptions, Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum Annotation {
		TAB_SET(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB_SET_NAME)),
		TAB_NAME(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB)),
		LAYOUT(new QName(UI_NAMESPACE_URI, UIOptionsConstants.LAYOUT)),
		COUNT_IN_SUMMARY_LIST(new QName(UI_NAMESPACE_URI, UIOptionsConstants.COUNT)),
		SHOW_ROW_NUMBERS(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_ROW_NUMBERS)),
		AUTOCOMPLETE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.AUTOCOMPLETE));
		
		private QName qName;

		private Annotation(QName qname) {
			this.qName = qname;
		}
		
		public QName getQName() {
			return qName;
		}
	}
	
	public enum Layout {
		FORM, TABLE
	}
	
	private List<UITabSet> tabSets;

	@Override
	public String getType() {
		return UI_TYPE;
	}

	public void init() {
		initParentRefernces();
	}

	protected void initParentRefernces() {
		if ( tabSets != null ) {
			for (UITabSet group : tabSets) {
				setParentInChildrenTabs(group);
			}
		}
	}

	protected void setParentInChildrenTabs(UITabSet group) {
		List<UITab> tabs = group.getTabs();
		for (UITab uiTab : tabs) {
			uiTab.setParent(group);
			setParentInChildrenTabs(uiTab);
		}
	}
	
	public List<UITabSet> getTabSets() {
		return CollectionUtil.unmodifiableList(tabSets);
	}
	
	public UITab getTab(NodeDefinition nodeDefn) {
		return getTab(nodeDefn, true);
	}
	
	public UITab getTab(NodeDefinition nodeDefn, boolean includeInherited) {
		UITab tab = null;
		EntityDefinition rootEntity = nodeDefn.getRootEntity();
		UITabSet tabSet = getTabSet(rootEntity);
		if ( tabSet != null ) {
			String tabName = nodeDefn.getAnnotation(Annotation.TAB_NAME.getQName());
			NodeDefinition parentDefn = nodeDefn.getParentDefinition();
			if ( StringUtils.isNotBlank(tabName) && ( parentDefn == null || parentDefn.getParentDefinition() == null ) ) {
				tab = tabSet.getTab(tabName);
			} else if ( parentDefn != null ) {
				UITab parentTab = getTab(parentDefn);
				if ( parentTab != null && StringUtils.isNotBlank(tabName) ) {
					tab = parentTab.getTab(tabName);
				} else if ( includeInherited ) {
					tab = parentTab;
				}
			}
		}
		return tab;
	}

	public UITabSet getTabSet(EntityDefinition rootEntityDefn) {
		String tabDefnName = rootEntityDefn.getAnnotation(Annotation.TAB_SET.getQName());
		UITabSet tabSet = getTabSet(tabDefnName);
		return tabSet;
	}
	
	public List<UITab> getAllowedTabs(NodeDefinition nodeDefn) {
		EntityDefinition rootEntity = nodeDefn.getRootEntity();
		UITabSet tabSet = getTabSet(rootEntity);
		if ( tabSet != null ) {
			NodeDefinition parentDefn = nodeDefn.getParentDefinition();
			if ( parentDefn == null || parentDefn.getParentDefinition() == null ) {
				return tabSet.getTabs();
			} else {
				UITab parentTab = getTab(parentDefn);
				if ( parentTab != null ) {
					return parentTab.getTabs();
				}
			}
		}
		return Collections.emptyList();
	}

	public boolean isAssignableTo(NodeDefinition nodeDefn, UITab tab) {
		List<UITab> allowedTabs = getAllowedTabs(nodeDefn);
		boolean result = allowedTabs.contains(tab);
		return result;
	}

	public List<NodeDefinition> getNodesPerTab(CollectSurvey survey, UITab tab, boolean includeChildrenOfEntities) {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		UITabSet tabSet = tab.getRootTabSet();
		EntityDefinition rootEntity = getRootEntityDefinition(survey, tabSet);
		Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>();
		queue.addAll(rootEntity.getChildDefinitions());
		while ( ! queue.isEmpty() ) {
			NodeDefinition defn = queue.remove();
			UITab nodeTab = getTab(defn);
			boolean nodeInTab = false;
			if ( nodeTab != null && nodeTab.equals(tab) ) {
				result.add(defn);
				nodeInTab = true;
			}
			if ( defn instanceof EntityDefinition && (includeChildrenOfEntities || !nodeInTab) ) {
				queue.addAll(((EntityDefinition) defn).getChildDefinitions());
			}
		}
		return result;
	}

	public void associateWithTab(NodeDefinition nodeDefn, UITab tab) {
		String tabName = tab.getName();
		nodeDefn.setAnnotation(Annotation.TAB_NAME.getQName(), tabName);
	}
	
	public void removeTabAssociation(NodeDefinition nodeDefn) {
		nodeDefn.setAnnotation(Annotation.TAB_NAME.getQName(), null);
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
		return layoutValue != null ? Layout.valueOf(layoutValue.toUpperCase()): null;
	}

	public void setLayout(NodeDefinition nodeDefn, Layout layout) {
		String layoutValue = layout != null ? layout.name().toLowerCase(): null;
		nodeDefn.setAnnotation(Annotation.LAYOUT.getQName(), layoutValue);
	}

	public EntityDefinition getRootEntityDefinition(CollectSurvey survey,
			UITabSet tabSet) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition defn : rootEntityDefinitions) {
			UITabSet entityTabSet = getTabSet(defn);
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
