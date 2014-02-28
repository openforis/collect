package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.schedule.CollectJob;
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
public class JobStatusPopUpVM extends BaseVM {

	public static final String JOB_FAILED_COMMAND = "jobFailed";
	public static final String JOB_COMPLETED_COMMAND = "jobCompleted";
	public static final String JOB_ABORTED_COMMAND = "jobAborted";
	public static final String UPDATE_PROGRESS_COMMAND = "updateProgress";
	
	public static final String JOB_ARG = "job";
	public static final String MESSAGE_ARG = "message";
	public static final String CANCELABLE_ARG = "cancelable";
	
	private String message;
	private boolean cancelable;

	private CollectJob<?> job;
	
	public static <J extends CollectJob<J>> Window openPopUp(String message, J job, boolean cancelable) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(JobStatusPopUpVM.MESSAGE_ARG, message);
		args.put(JobStatusPopUpVM.JOB_ARG, job);
		args.put(JobStatusPopUpVM.CANCELABLE_ARG, cancelable);
		Window popUp = PopUpUtil.openPopUp(Resources.Component.JOB_STATUS_POPUP.getLocation(), true, args);
		return popUp;
	}
	
	@Init
	public <J extends CollectJob<J>> void init(@ExecutionArgParam(MESSAGE_ARG) String message, 
			@ExecutionArgParam(JOB_ARG) J job,
			@ExecutionArgParam(CANCELABLE_ARG) boolean cancelable) {
		this.message = message;
		this.job = job;
		this.cancelable = cancelable;
	}
	
	@GlobalCommand
	public void updateProgress() {
		switch ( job.getStatus() ) {
		case COMPLETED:
			BindUtils.postGlobalCommand(null, null, JOB_COMPLETED_COMMAND, null);
			break;
		case FAILED:
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("errorMessage", job.getLastException().getMessage());
			BindUtils.postGlobalCommand(null, null, JOB_FAILED_COMMAND, args);
			break;
		case ABORTED:
			dispatchJobAbortedCommand();
			break;
		default:
		}
		notifyChange("progress");
	}

	private void dispatchJobAbortedCommand() {
		BindUtils.postGlobalCommand(null, null, JOB_ABORTED_COMMAND, null);
	}
	
	public int getProgress() {
		return job.getProgressPercent();
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isCancelable() {
		return cancelable;
	}
	
	@Command
	public void abort() {
		job.abort();
		dispatchJobAbortedCommand();
	}
	
	
}
