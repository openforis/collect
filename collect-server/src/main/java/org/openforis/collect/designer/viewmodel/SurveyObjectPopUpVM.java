package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.util.MessageUtil;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

public abstract class SurveyObjectPopUpVM<T> extends SurveyObjectBaseVM<T> {
	
	@Command
	public void close(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		event.stopPropagation();
		if (!isCurrentFormValid()) {
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				public void onOk() {
					cancel();
				}
			}, "global.unapplied_changes_made", null, "global.unsaved_changes", (Object[]) null, 
					"global.continue_and_loose_changes", "global.stay_on_this_page");
		} else {
			cancel();
		}
	}

	@Command
	public void apply(@ContextParam(ContextType.VIEW) final Component view,
			@ContextParam(ContextType.BINDER) final Binder binder) {
		dispatchApplyChangesCommand(binder);
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			public void onOk(boolean ignoreUnsavedChanges) {
				if (ignoreUnsavedChanges) {
					undoLastChanges(view);
				} else {
					commitChanges(binder);
				}
				dispatchChangesAppliedCommand(ignoreUnsavedChanges);
			}
		});
	}

	@Command
	public void cancel() {
		undoLastChanges();
		dispatchChangesCancelledCommand();
	}
	
	protected abstract void dispatchChangesCancelledCommand();

	protected abstract void dispatchChangesAppliedCommand(boolean ignoreUnsavedChanges);

}
