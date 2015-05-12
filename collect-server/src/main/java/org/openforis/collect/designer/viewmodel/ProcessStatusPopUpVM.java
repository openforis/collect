package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.process.AbstractProcess;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@Deprecated
public class ProcessStatusPopUpVM extends BaseVM {

	public static final String UPDATE_PROGRESS_COMMAND = "updateProgress";
	
	public static final String PROCESS_ARG = "process";
	public static final String MESSAGE_ARG = "message";
	public static final String CANCELABLE_ARG = "cancelable";
	
	private AbstractProcess<?, ?> process;

	private String message;
	private boolean cancelable;
	
	public static Window openPopUp(String message, AbstractProcess<?, ?> process, boolean cancelable) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(ProcessStatusPopUpVM.MESSAGE_ARG, message);
		args.put(ProcessStatusPopUpVM.PROCESS_ARG, process);
		args.put(ProcessStatusPopUpVM.CANCELABLE_ARG, cancelable);
		Window popUp = PopUpUtil.openPopUp(Resources.Component.PROCESS_STATUS_POPUP.getLocation(), true, args);
		return popUp;
	}
	
	@Init
	public void init(@ExecutionArgParam(MESSAGE_ARG) String message, 
			@ExecutionArgParam(PROCESS_ARG) AbstractProcess<?, ?> process,
			@ExecutionArgParam(CANCELABLE_ARG) boolean cancelable) {
		this.message = message;
		this.process = process;
		this.cancelable = cancelable;
	}
	
	@GlobalCommand
	public void updateProgress() {
		switch ( process.getStatus().getStep() ) {
		case COMPLETE:
			BindUtils.postGlobalCommand(null, null, "processComplete", null);
			break;
		case ERROR:
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("errorMessage", process.getStatus().getErrorMessage());
			BindUtils.postGlobalCommand(null, null, "processError", args);
			break;
		case CANCEL:
			BindUtils.postGlobalCommand(null, null, "processCancelled", null);
			break;
		default:
		}
		notifyChange("progress");
	}
	
	public int getProgress() {
		return process.getStatus().getProgressPercent();
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isCancelable() {
		return cancelable;
	}
	
	@Command
	public void cancel() {
		process.cancel();
		BindUtils.postGlobalCommand(null, null, "processCancelled", null);
	}
	
	
}
