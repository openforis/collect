package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.viewmodel.SurveyBaseVM;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.BindUtils;
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
public class SurveyEditComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		Selectors.wireEventListeners(comp, this);
	}
	
	@Listen("onSwitchTab = tab")
	public void onSwitchTab(Event event) throws InterruptedException {
		SurveyBaseVM vm = (SurveyBaseVM) getViewModel();
		final Tab tab = (Tab) event.getTarget();
		vm.checkCanLeaveForm(new SurveyBaseVM.CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				doSelectTab(tab, confirmed);
			}
		});
	}
	
	protected void doSelectTab(final Tab tab, boolean undoChanges) {
		if ( undoChanges ) {
			BindUtils.postGlobalCommand(null, null, SurveyBaseVM.UNDO_LAST_CHANGES_GLOBAL_COMMAND, null);
		}
		Tabbox tabbox = tab.getTabbox();
		tabbox.setSelectedTab(tab);
	}
}
