package org.openforis.collect.designer.composer;

import java.util.Set;

import org.openforis.collect.designer.component.AbstractTreeModel.AbstractNode;
import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.viewmodel.SchemaVM;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySchemaEditComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;
	
	@Wire
	private Tree nodesTree;
	@Wire
	private Menupopup entityPopup;
	@Wire
	private Menupopup attributePopup;
	@Wire
	private Menupopup tableEntityPopup;
	@Wire
	private Menupopup detachedNodePopup;
	
	
	@Override
	public void doAfterCompose(Component view) throws Exception {
		super.doAfterCompose(view);
		Selectors.wireEventListeners(view, this);
		Selectors.wireComponents(view, this, false);
		//nodesTree.setItemRenderer(new SchemaTreeItemRenderer());
	}
	
	@Listen("onSelectTreeNode")
	public void onSelectTreeNode(Event event) throws InterruptedException {
		SchemaVM vm = (SchemaVM) getViewModel();
		if ( vm.checkCanLeaveForm() ) {
			Tab tab = (Tab) event.getTarget();
			Tabbox tabbox = tab.getTabbox();
			tabbox.setSelectedTab(tab);
		}
	}
	
	public void updateNodeLabel(SurveyObject surveyObject, String label) {
		TreeModel<?> treeModel = nodesTree.getModel();
		SchemaTreeModel schemaTreeModel = (SchemaTreeModel) treeModel;
		schemaTreeModel.updateNodeLabel(surveyObject, label);
	}

	protected SchemaNodeData getSelectedNodeData() {
		TreeModel<?> treeModel = nodesTree.getModel();
		Set<TreeNode<SchemaNodeData>> selection = ((SchemaTreeModel) treeModel).getSelection();
		TreeNode<SchemaNodeData> selectedNode = selection.iterator().next();
		SchemaNodeData data = selectedNode.getData();
		return data;
	}
	
	public void refreshSelectedTreeNodeContextMenu() {
		SchemaVM viewModel = (SchemaVM) getViewModel();
		Treeitem selectedItem = nodesTree.getSelectedItem();
		SchemaNodeData data = getSelectedNodeData();
		if ( data instanceof SchemaNodeData ) {
			Menupopup popupMenu;
			if ( data.isDetached() ) { 
				popupMenu = detachedNodePopup;
			} else if ( viewModel.isEntity(data) ) {
				if ( viewModel.isTableEntity(data)) {
					popupMenu = tableEntityPopup;
				} else {
					popupMenu = entityPopup;
				}
			} else {
				popupMenu = attributePopup;
			}
			selectedItem.setContext(popupMenu);
		}
	}
	
	static class SchemaTreeItemRenderer implements TreeitemRenderer<AbstractNode<SchemaNodeData>> {

		@Override
		public void render(Treeitem item, AbstractNode<SchemaNodeData> node, int index)
				throws Exception {
			SchemaNodeData data = node.getData();
			Treerow row = new Treerow();
			Treecell cell = new Treecell();
			SurveyObject surveyObject = data.getSurveyObject();
			if ( surveyObject instanceof NodeDefinition ) {
				//schema node
				cell.setLabel(((NodeDefinition) surveyObject).getName());
			} else {
				//tab
				Textbox textbox = new Textbox();
				cell.appendChild(textbox);
			}
			cell.setImage(SchemaVM.getIcon(data));
			row.appendChild(cell);
			item.appendChild(row);
		}
		
	}
}
