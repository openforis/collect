package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.component.BasicTreeModel.AbstractNode;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.viewmodel.SchemaVM;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
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
			cell.setImage(SchemaVM.getIcon(data.getSurveyObject()));
			row.appendChild(cell);
			item.appendChild(row);
		}
		
	}
}
