/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.component.SurveyObjectTreeModelCreator;
import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.component.UITreeModelCreator;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class SchemaTreePopUpVM extends SurveyBaseVM {

	public static final String SCHEMA_TREE_NODE_SELECTED_COMMAND = "schemaTreeNodeSelected";
	
	private SchemaTreeModel treeModel;
	private SurveyObject selectedNode;

	private Predicate<SurveyObject> selectableNodePredicate;
	private Predicate<SurveyObject> disabledNodePredicate;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("rootEntity") EntityDefinition rootEntity, 
			@ExecutionArgParam("version") ModelVersion version,
			@ExecutionArgParam("includedNodePredicate") Predicate<SurveyObject> includedNodePredicate,
			@ExecutionArgParam("includeEmptyNodes") boolean includeEmtptyNodes,
			@ExecutionArgParam("disabledNodePredicate") Predicate<SurveyObject> disabledNodePredicate,
			@ExecutionArgParam("selectableNodePredicate") Predicate<SurveyObject> selectableNodePredicate,
			@ExecutionArgParam("selection") SurveyObject selection) {
		super.init();
		SurveyObjectTreeModelCreator modelCreator = new UITreeModelCreator(version, disabledNodePredicate, includedNodePredicate, includeEmtptyNodes, currentLanguageCode);
		this.treeModel = modelCreator.createModel(rootEntity);
		this.treeModel.openAllItems();
		this.selectableNodePredicate = selectableNodePredicate;
		this.disabledNodePredicate = disabledNodePredicate;
		if ( selection != null ) {
			this.selectedNode = selection;
			this.treeModel.select(selection);
			this.treeModel.showSelectedNode();
		}
	}
	
	public static Window openPopup(String title, EntityDefinition rootEntity, ModelVersion version,  
			Predicate<SurveyObject> includedNodePredicate, boolean includeEmptyNodes, Predicate<SurveyObject> disabledNodePredicate,
			Predicate<SurveyObject> selectableNodePredicate, SurveyObject selection) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("rootEntity", rootEntity);
		args.put("version", version);
		args.put("title", title);
		args.put("disabledNodePredicate", disabledNodePredicate);
		args.put("includedNodePredicate", includedNodePredicate);
		args.put("selectableNodePredicate", selectableNodePredicate);
		args.put("selection", selection);
		args.put("includeEmptyNodes", includeEmptyNodes);
		
		return openPopUp(Resources.Component.SCHEMA_TREE_POPUP.getLocation(), true, args);
	}
	
	@Command
	public void apply(@BindingParam("selectedSurveyObject") SurveyObject selectedSurveyObject) {
		if ( ( disabledNodePredicate == null || ! disabledNodePredicate.evaluate(selectedSurveyObject) ) &&
				( selectableNodePredicate == null || selectableNodePredicate.evaluate(selectedSurveyObject) ) ) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("node", selectedSurveyObject);
			BindUtils.postGlobalCommand(null, null, SCHEMA_TREE_NODE_SELECTED_COMMAND, args);
		}
	}
	
	@Command
	public void cancel() {
		BindUtils.postGlobalCommand(null, null, "closeSchemaNodeSelector", null);
	}
	
	@Command
	public void nodeSelected(@BindingParam("node") SurveyObject surveyObject) {
		if ( selectableNodePredicate != null && ! selectableNodePredicate.evaluate(surveyObject) ) {
			//deselect node
			treeModel.select(selectedNode);
		} else if ( selectedNode == surveyObject ) {
			treeModel.clearSelection();
			selectedNode = null;
		} else {
			selectedNode = surveyObject;
		}
	}
	
	public SchemaTreeModel getTreeModel() {
		return treeModel;
	}
	
}
