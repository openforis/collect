package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.viewmodel.SurveySchemaEditVM;
import org.zkoss.bind.BindComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySchemaEditComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		Selectors.wireEventListeners(comp, this);
	}
	
	@Listen("onSelectTreeNode")
	public void onSelectTreeNode(Event event) throws InterruptedException {
		SurveySchemaEditVM vm = (SurveySchemaEditVM) getViewModel();
		if ( vm.checkCurrentFormValid() ) {
			Tab tab = (Tab) event.getTarget();
			Tabbox tabbox = tab.getTabbox();
			tabbox.setSelectedTab(tab);
		}
	}
}
