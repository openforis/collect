package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openforis.collect.designer.component.UITabsTreeModel;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaLayoutSimpleVM extends SurveyBaseVM {

	private static final String TAB_SET_CHANGED_GLOBAL_COMMAND = "tabSetChanged";
	private static final String TAB_CHANGED_GLOBAL_COMMAND = "tabChanged";
	private static final String TAB_NAME_PREFIX = "tab_";
	private static final String TAB_NAME_SEPARATOR = "_";

	private EntityDefinition selectedRootEntity;
	private UITabSet rootTabSet;
	private UITabsTreeModel treeModel;
	private UITab selectedTab;
	private ModelVersion selectedVersion;

	@GlobalCommand
	@NotifyChange({"nodes"})
	public void schemaChanged() {
		initTreeModel();
		tabSelected(null);
	}
	
	@Command
	@NotifyChange({"selectedRootEntity","treeModel"})
	public void rootEntitySelected(@BindingParam("rootEntity") EntityDefinition rootEntity) {
		selectedRootEntity = rootEntity;
		initTreeModel();
	}
	
	@Command
	@NotifyChange({"selectedVersion","treeModel"})
	public void versionSelected(@BindingParam("version") ModelVersion version) {
		selectedVersion = version;
		initTreeModel();
	}
	
	@Command
	@NotifyChange({"selectedTab"})
	public void tabSelected(@BindingParam("tab") UITab tab) {
		selectedTab = tab;
		if ( treeModel != null ) {
			treeModel.select(tab);
		}
//		List<ModelVersion> versions = survey.getVersions();
//		setFormVersion(versions.isEmpty() ? null: versions.get(0));
//		UITabSet tabSet = getRootTabSet(node);
//		dispatchTabSetChangedCommand();
	}

	@Command
	@NotifyChange({"treeModel","selectedTab"})
	public void addTab(@BindingParam("parent") UITabSet parent) {
		UITab tab = parent.createTab();
		String tabName = generateNewTabName(parent);
		tab.setName(tabName);
		tab.setLabel(currentLanguageCode, tabName);
		parent.addTab(tab);
		selectedTab = tab;
		treeModel.appendNodeToSelected(tab);
	}
	
	@Command
	public void removeTab() {
		String confirmMessageKey = null;
		if ( ! selectedTab.getTabs().isEmpty() ) {
			confirmMessageKey = "survey.layout.tab.remove.confirm.nested_tabs_present";
		} else {
			CollectSurvey survey = getSurvey();
			UIOptions uiOpts = survey.getUIOptions();
			List<NodeDefinition> nodesPerTab = uiOpts.getNodesPerTab(selectedTab, false);
			if ( ! nodesPerTab.isEmpty() ) {
				confirmMessageKey = "survey.layout.tab.remove.confirm.associated_nodes_present";
			}
		}
		if ( confirmMessageKey != null ) {
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performRemoveSelectedTab();
				}
			}, confirmMessageKey);
		} else {
			performRemoveSelectedTab();
		}
	}

	protected void performRemoveSelectedTab() {
		UITabSet parent = selectedTab.getParent();
		parent.removeTab(selectedTab);
		treeModel.removeSelectedNode();
		selectedTab = null;
		notifyChange("treeModel", "selectedTab");
		dispatchTabSetChangedCommand();
	}

	@Command
	public void tabChanged(@BindingParam("tab") UITab tab, @BindingParam("label") String label) {
		if ( validateTabLabel(label) ) {
			performUpdateTabLabel(tab, label);
		}
	}

	protected void performUpdateTabLabel(UITab tab, String label) {
		tab.setLabel(currentLanguageCode, label.trim());
		dispatchTabChangedCommand(tab);
		BindUtils.postNotifyChange(null, null, tab, "*");
	}
	
	protected boolean validateTabLabel(String label) {
		if ( StringUtils.isBlank(label) ) {
			MessageUtil.showWarning("survey.layout.tab.label.error.required");
			return false;
		} else {
			return true;
		}
	}
	
	@Command
	public void showPreview() {
		Execution current = Executions.getCurrent();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("preview", "true"));
		params.add(new BasicNameValuePair("surveyId", Integer.toString(survey.getId())));
		params.add(new BasicNameValuePair("rootEntityId", Integer.toString(selectedRootEntity.getId())));
		params.add(new BasicNameValuePair("versionId", Integer.toString(selectedVersion.getId())));
		String uri = Resources.PREVIEW_PATH + "?" + URLEncodedUtils.format(params, "UTF-8");
		current.sendRedirect(uri, "_blank");
	}
	
	public String getTabLabel(UITab tab) {
		return tab != null ? tab.getLabel(currentLanguageCode): null;
	}
	
	private String generateNewTabName(UITabSet parentGroup) {
		String prefix = TAB_NAME_PREFIX;
		Stack<Integer> parts = new Stack<Integer>();
		UITabSet currentGroup = parentGroup;
		do {
			int position = currentGroup.getTabs().size() + 1;
			parts.push(position);
			currentGroup = currentGroup.getParent();
		} while ( currentGroup != null );
		String suffix = StringUtils.join(parts.toArray(), TAB_NAME_SEPARATOR);
		String tabName = prefix + suffix;
		return tabName;
	}
	
	protected void dispatchTabSetChangedCommand() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tabSet", rootTabSet);
		BindUtils.postGlobalCommand(null, null, TAB_SET_CHANGED_GLOBAL_COMMAND, args);
	}
	
	protected void dispatchTabChangedCommand(UITab tab) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tab", tab);
		BindUtils.postGlobalCommand(null, null, TAB_CHANGED_GLOBAL_COMMAND, args);
	}

	@Command
	@NotifyChange({"treeModel","moveItemUpDisabled","moveItemDownDisabled"})
	public void moveItemUp() {
		moveItem(true);
	}
	
	@Command
	@NotifyChange({"treeModel","moveItemUpDisabled","moveItemDownDisabled"})
	public void moveItemDown() {
		moveItem(false);
	}
	
	protected void moveItem(boolean up) {
		List<UITab> siblings = selectedTab.getSiblings();
		int oldIndex = siblings.indexOf(selectedTab);
		int newIndex = up ? oldIndex - 1: oldIndex + 1;
		moveItem(newIndex);
	}
	
	protected void moveItem(int newIndex) {
		UITabSet parent = selectedTab.getParent();
		parent.moveTab(selectedTab, newIndex);
		treeModel.moveSelectedNode(newIndex);
		dispatchTabSetChangedCommand();
	}
	
	@DependsOn("selectedTab")
	public boolean isMoveItemUpDisabled() {
		if ( selectedTab != null ) {
			int index = selectedTab.getIndex();
			return index <= 0;
		} else {
			return true;
		}
	}
	
	@DependsOn("selectedTab")
	public boolean isMoveItemDownDisabled() {
		return isMoveNodeDisabled(false);
	}
	
	protected boolean isMoveNodeDisabled(boolean up) {
		if ( selectedTab != null ) {
			List<UITab> siblings = selectedTab.getSiblings();
			int index = siblings.indexOf(selectedTab);
			return isMoveItemDisabled(siblings, index, up);
		} else {
			return true;
		}
	}

	protected boolean isMoveItemDisabled(List<?> siblings, int index, boolean up) {
		return up ? index <= 0: index < 0 || index >= siblings.size() - 1;
	}
	
	public UITabsTreeModel getTreeModel() {
		if ( treeModel == null ) {
			initTreeModel();
		}
		return treeModel;
    }
	
	protected void initTreeModel() {
		if ( selectedRootEntity == null ) {
			rootTabSet = null;
			treeModel = null;
		} else {
			CollectSurvey survey = getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			rootTabSet = uiOptions.getTabSet(selectedRootEntity);
			treeModel = UITabsTreeModel.createInstance(rootTabSet);
		}
	}

	public List<EntityDefinition> getRootEntities() {
		CollectSurvey survey = getSurvey();
		Schema schema = survey.getSchema();
		List<EntityDefinition> result = schema.getRootEntityDefinitions();
		return result;
	}
	
	public List<ModelVersion> getFormVersions() {
		CollectSurvey survey = getSurvey();
		List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
		return new BindingListModelList<ModelVersion>(result, false);
	}
	
	public UITabSet getRootTabSet() {
		return rootTabSet;
	}
	
	public EntityDefinition getSelectedRootEntity() {
		return selectedRootEntity;
	}

	public void setSelectedRootEntity(EntityDefinition selectedRootEntity) {
		this.selectedRootEntity = selectedRootEntity;
	}

	public UITab getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(UITab selectedTab) {
		this.selectedTab = selectedTab;
	}

	public ModelVersion getSelectedVersion() {
		return selectedVersion;
	}

	public void setSelectedVersion(ModelVersion selectedVersion) {
		this.selectedVersion = selectedVersion;
	}
	
}
