package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.concurrency.Job;
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

	private Job job;
	
	public static <J extends Job> Window openPopUp(String message, J job, boolean cancelable) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(JobStatusPopUpVM.MESSAGE_ARG, message);
		args.put(JobStatusPopUpVM.JOB_ARG, job);
		args.put(JobStatusPopUpVM.CANCELABLE_ARG, cancelable);
		return PopUpUtil.openPopUp(Resources.Component.JOB_STATUS_POPUP.getLocation(), true, args);
	}
	
	@Init
	public <J extends Job> void init(@ExecutionArgParam(MESSAGE_ARG) String message, 
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
			dispatchJobCompletedCommand();
			break;
		case FAILED:
			dispatchJobFailedCommand();
			break;
		case ABORTED:
			dispatchJobAbortedCommand();
			break;
		default:
		}
		notifyChange("progress");
	}

	private void dispatchJobCompletedCommand() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("job", job);
		BindUtils.postGlobalCommand(null, null, JOB_COMPLETED_COMMAND, args);
	}

	private void dispatchJobFailedCommand() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("job", job);
		BindUtils.postGlobalCommand(null, null, JOB_FAILED_COMMAND, args);
	}

	private void dispatchJobAbortedCommand() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("job", job);
		BindUtils.postGlobalCommand(null, null, JOB_ABORTED_COMMAND, args);
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
