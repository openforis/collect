/**
 * 
 */
package org.openforis.collect.designer.composer;

import org.openforis.collect.model.ui.UITab;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupPanelComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;
	
	@Wire
	private Include listOfNodesInclude;

	private UITab tab;
	
	@Init
	public void init(@ExecutionArgParam("tab") UITab tab) {
		this.tab = tab;
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
//		listOfNodesInclude = (Include) comp.getFirstChild();
//		listOfNodesInclude.setDynamicProperty("tab", tab);
//		listOfNodesInclude.setSrc("/view/designer/component/schema_layout/editablenodeslist.zul");
	}
	

}
