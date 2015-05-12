/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.collect.model.ui.UITabsGroup;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyConfigurationEditVM extends SurveyEditBaseVM {

	private static final String TAB_DEFINITIONS_UPDATED_COMMAND = "tabDefinitionsUpdated";
	private UITabsGroup selectedTab;
	private UITabDefinition editedTabDefinition;
	private UITab editedTab;
	private UIConfigurationTreeModel treeModel;
	private boolean newTab;
	
	public SurveyConfigurationEditVM() {
		newTab = true;
	}
	
	@Command
	@NotifyChange({"newTab","selectedTab","editedTab","editedTabDefinition"})
	public void tabSelected(@BindingParam("node") Treeitem node) {
		selectedTab = null; 
		editedTab = null; 
		editedTabDefinition = null;
		newTab = false;
		if ( node != null ) {
			TreeNode<UITabsGroup> treeNode = node.getValue();
			selectedTab = treeNode.getData();
			if ( selectedTab instanceof UITabDefinition ) {
				editedTabDefinition = (UITabDefinition) selectedTab;
			} else {
				editedTab = (UITab) selectedTab;
			}
		}
	}
	
	@Command
	@NotifyChange({"newTab","editedTab","editedTabDefinition"})
	public void addTabDefinition() {
		editedTab = null;
		editedTabDefinition = new UITabDefinition();
		newTab = true;
	}
	
	@Command
	@NotifyChange({"newTab","editedTabDefinition","editedTab"})
	public void addTab() {
		editedTabDefinition = null;
		editedTab = new UITab();
		newTab = true;
	}
	
	@Command
	@NotifyChange({"editedTabDefinition","editedTab","selectedTab"})
	public void applyChanges() {
		CollectSurvey survey = getSurvey();
		UIConfiguration uiConfiguration = survey.getUIConfiguration();
		if ( newTab ) {
			if ( editedTabDefinition != null ) {
				uiConfiguration.addTabDefinition(editedTabDefinition);
				treeModel.appendToSelected(editedTabDefinition);
				selectedTab = editedTabDefinition;
			} else if ( editedTab != null) {
				selectedTab.addTab(editedTab);
				treeModel.appendToSelected(editedTab);
				selectedTab = editedTab;
			}
		} else {
			String tabName = selectedTab.getName();
			if ( editedTabDefinition != null ) {
				String newName = editedTabDefinition.getName();
				UITabDefinition newTabDefn = uiConfiguration.updateTabDefinition(tabName, newName);
				//TODO avoid use of side effect...
				//treeModel.updateSelectedNode(newTabDefn);
				selectedTab = newTabDefn;
			} else {
				selectedTab.setName(editedTab.getName());
				((UITab) selectedTab).setLabel(editedTab.getLabel());
				//TODO avoid use of side effect...
				//treeModel.updateSelectedNode(editedTab);
			}
		}
		newTab = false;
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("tab", selectedTab);
		BindUtils.postGlobalCommand(null, null, TAB_DEFINITIONS_UPDATED_COMMAND, args);
	}
	
	public DefaultTreeModel<UITabsGroup> getTabs() {
		if ( treeModel == null ) {
			UIConfiguration uiConfig = getSurvey().getUIConfiguration();
			treeModel = new UIConfigurationTreeModel(uiConfig);
		}
		return treeModel.getModel();
    }
	
	public UITabsGroup getSelectedTab() {
		return selectedTab;
	}

	public UITab getEditedTab() {
		return editedTab;
	}
	
	public UITabDefinition getEditedTabDefinition() {
		return editedTabDefinition;
	}
	
	protected static class UIConfigurationTreeModel {
		
		private DefaultTreeModel<UITabsGroup> model;
		
		public UIConfigurationTreeModel(UIConfiguration uiConfig) {
			init(uiConfig);
		}

		public void init(UIConfiguration uiConfiguration) {
			List<UITabDefinition> tabDefns = uiConfiguration.getTabDefinitions();
			List<TreeNode<UITabsGroup>> treeNodes = UITabTreeNode.fromList(tabDefns);
			TreeNode<UITabsGroup> root = new UITabTreeNode(null, treeNodes);
			model = new DefaultTreeModel<UITabsGroup>(root);
		}
		
		public DefaultTreeModel<UITabsGroup> getModel() {
			return model;
		}

		protected void removeSelectedNode() {
			int[] selectionPath = model.getSelectionPath();
			TreeNode<UITabsGroup> treeNode = model.getChild(selectionPath);
			TreeNode<UITabsGroup> parentTreeNode = treeNode.getParent();
			parentTreeNode.remove(treeNode);
		}
		
		protected void updateSelectedNode(UITabsGroup newTab) {
			int[] selectionPath = model.getSelectionPath();
			TreeNode<UITabsGroup> treeNode = model.getChild(selectionPath);
			treeNode.setData(newTab);
		}
		
		protected void appendToSelected(UITabsGroup item) {
			UITabTreeNode treeNode = new UITabTreeNode(item);
			int[] selectionPath = model.getSelectionPath();
			if ( selectionPath == null || item instanceof UITabDefinition ) {
				TreeNode<UITabsGroup> root = model.getRoot();
				root.add(treeNode);
			} else {
				TreeNode<UITabsGroup> selectedTreeNode = model.getChild(selectionPath);
				selectedTreeNode.add(treeNode);
			}
			model.addOpenObject(treeNode.getParent());
			model.setSelection(Arrays.asList(treeNode));
		}
		
		protected static class UITabTreeNode extends DefaultTreeNode<UITabsGroup> {
			
			private static final long serialVersionUID = 1L;
			
			public UITabTreeNode(UITabsGroup data) {
				this(data, null);
			}
			
			public UITabTreeNode(UITabsGroup data, Collection<TreeNode<UITabsGroup>> children) {
				super(data, children);
			}
			
			public static List<TreeNode<UITabsGroup>> fromList(List<? extends UITabsGroup> items) {
				List<TreeNode<UITabsGroup>> result = null;
				if ( items != null ) {
					result = new ArrayList<TreeNode<UITabsGroup>>();
					for (UITabsGroup item : items) {
						List<TreeNode<UITabsGroup>> childrenNodes = null;
						List<UITab> childrenTabs = item.getTabs();
						childrenNodes = fromList(childrenTabs);
						UITabTreeNode node = new UITabTreeNode(item, childrenNodes);
						result.add(node);
					}
				}
				return result;
			}
			
		}
	}

}
