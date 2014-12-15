package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.component.UITabsTreeModel;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaLayoutSimpleVM extends SurveyBaseVM {

	private static final String TAB_SET_CHANGED_GLOBAL_COMMAND = "tabSetChanged";
	private static final String TAB_CHANGED_GLOBAL_COMMAND = "tabChanged";

	private EntityDefinition selectedRootEntity;
	private UITabSet rootTabSet;
	private UITabsTreeModel treeModel;
	private UITab selectedTab;
	private ModelVersion selectedVersion;

	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		//if one root entity is defined, select it
		List<EntityDefinition> rootEntities = getRootEntities();
		if (rootEntities.size() == 1) {
			EntityDefinition mainRootEntity = rootEntities.get(0);
			performNodeTreeFilterChange(mainRootEntity, null);
		}
	}
	
	@GlobalCommand
	@NotifyChange({"rootEntities","treeModel"})
	public void schemaChanged() {
		checkSelectedRootEntityExistence();
		initTreeModel();
		selectTab(null);
	}

	protected void checkSelectedRootEntityExistence() {
		if ( selectedRootEntity != null ) {
			List<EntityDefinition> rootEntities = getRootEntities();
			if ( rootEntities != null && ! rootEntities.contains(selectedRootEntity) ) {
				selectedRootEntity = null;
			}
		}
	}
	
	@Command
	public void rootEntitySelected(@BindingParam("rootEntity") EntityDefinition rootEntity) {
		performNodeTreeFilterChange(rootEntity, selectedVersion);
	}
	
	@Command
	public void versionSelected(@BindingParam("version") ModelVersion version) {
		performNodeTreeFilterChange(selectedRootEntity, version);
	}
	
	private void performNodeTreeFilterChange(EntityDefinition rootEntity, ModelVersion version) {
		selectedRootEntity = rootEntity;
		selectedVersion = version;
		initTreeModel();
		notifyChange("selectedRootEntity", "selectedVersion", "treeModel");
	}
	
	@Override
	@GlobalCommand
	@NotifyChange({"treeModel"})
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		initTreeModel();
	}
	
	@Command
	@NotifyChange({"selectedTab"})
	public void selectTab(@BindingParam("tab") UITab tab) {
		selectedTab = tab;
		if ( treeModel != null ) {
			treeModel.select(tab);
		}
	}

	@Command
	@NotifyChange({"treeModel","selectedTab"})
	public void addTab(@BindingParam("parent") UITabSet parent) {
		if ( rootTabSet != null ) {
			if ( parent == null ) {
				parent = rootTabSet;
				treeModel.deselect();
			}
			CollectSurvey survey = getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			UITab tab = uiOptions.createTab();
			String label = Labels.getLabel("survey.schema.node.layout.default_tab_label");
			tab.setLabel(currentLanguageCode, label);
			parent.addTab(tab);
			treeModel.appendNodeToSelected(tab);
			selectedTab = tab;
			dispatchTabSetChangedCommand();
		}
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
			MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performRemoveSelectedTab();
				}
			}, confirmMessageKey);
			params.setOkLabelKey("global.delete_item");
			MessageUtil.showConfirm(params);
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
	public void updateTabLabel(@BindingParam("tab") UITab tab, @BindingParam("label") String label) {
		if ( validateTabLabel(label) ) {
			tab.setLabel(currentLanguageCode, label.trim());
			dispatchTabChangedCommand(tab);
		}
	}
	
	protected boolean validateTabLabel(String label) {
		if ( StringUtils.isBlank(label) ) {
			MessageUtil.showWarning("survey.layout.tab.label.error.required");
			return false;
		} else {
			return true;
		}
	}
	
	public String getTabLabel(UITab tab) {
		if ( tab != null ) {
			String result = tab.getLabel(currentLanguageCode);
			return result;
		} else {
			return null;
		}
	}
	
	public String getDefaultLanugageTabLabel(UITab tab) {
		if ( tab == null ) {
			return null;
		} else {
			String defaultLanguage = getSurvey().getDefaultLanguage();
			String result = tab.getLabel(defaultLanguage);
			return result;
		}
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
		BindUtils.postNotifyChange(null, null, tab, "*");
	}

	@Command
	public void moveItemUp() {
		moveItem(true);
	}
	
	@Command
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
		notifyChange("treeModel","moveItemUpDisabled","moveItemDownDisabled");
		treeModel.select(selectedTab);
	}
	
	public boolean isMainTab(UITab tab) {
		UIOptions uiOptions = getUIOptions();
		return uiOptions.isMainTab(tab);
	}

	@DependsOn("selectedTab")
	public boolean isMoveItemUpDisabled() {
		return isMoveItemDisabled(true);
	}
	
	@DependsOn("selectedTab")
	public boolean isMoveItemDownDisabled() {
		return isMoveItemDisabled(false);
	}
	
	protected boolean isMoveItemDisabled(boolean up) {
		UIOptions uiOptions = getUIOptions();
		if ( uiOptions == null ) {
			//TODO session expired?!
			return true;
		} else if ( selectedTab == null || uiOptions.isMainTab(selectedTab) ) {
			return true;
		} else {
			List<UITab> siblings = selectedTab.getSiblings();
			int index = siblings.indexOf(selectedTab);
			if ( up ) {
				boolean willMoveOverTheMainTab = index == 1 && selectedTab.getDepth() == 1;
				return index <= 0 || willMoveOverTheMainTab;
			} else {
				return index < 0 || index >= siblings.size() - 1;
			}
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
			if ( survey == null ) {
				//TODO session expired...?
			} else {
				UIOptions uiOptions = survey.getUIOptions();
				rootTabSet = uiOptions.getAssignedRootTabSet(selectedRootEntity);
				treeModel = UITabsTreeModel.createInstance(rootTabSet);
			}
		}
	}

	public List<ModelVersion> getFormVersions() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired...?
			return null;
		} else {
			List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
			return new BindingListModelList<ModelVersion>(result, false);
		}
	}
	
	protected UIOptions getUIOptions() {
		CollectSurvey survey = getSurvey();
		return survey == null ? null: survey.getUIOptions();
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
