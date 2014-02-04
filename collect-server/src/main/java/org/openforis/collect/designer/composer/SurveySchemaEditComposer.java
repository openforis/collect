package org.openforis.collect.designer.composer;

import java.util.Set;

import org.openforis.collect.designer.component.AbstractTreeModel.NodeData;
import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.viewmodel.SchemaVM;
import org.zkoss.bind.BindComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

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
	
	public void refreshSelectedTreeNodeLabel(String label) {
		TreeModel<?> treeModel = nodesTree.getModel();
		((SchemaTreeModel) treeModel).setSelectedNodeLabel(label);
		Treeitem selectedItem = nodesTree.getSelectedItem();
		selectedItem.setLabel(label);
	}

	protected NodeData getSelectedNodeData() {
		TreeModel<?> treeModel = nodesTree.getModel();
		Set<TreeNode<NodeData>> selection = ((SchemaTreeModel) treeModel).getSelection();
		TreeNode<NodeData> selectedNode = selection.iterator().next();
		NodeData data = selectedNode.getData();
		return data;
	}
	
	public void refreshSelectedTreeNodeContextMenu() {
		SchemaVM viewModel = (SchemaVM) getViewModel();
		Treeitem selectedItem = nodesTree.getSelectedItem();
		NodeData data = getSelectedNodeData();
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
}
