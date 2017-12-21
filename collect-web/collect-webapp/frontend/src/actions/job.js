import ServiceFactory from 'services/ServiceFactory'

export const START_JOB_MONITOR = 'START_JOB_MONITOR'
export const CLOSE_JOB_MONITOR = 'CLOSE_JOB_MONITOR'
export const REQUEST_JOB = 'REQUEST_JOB'
export const RECEIVE_JOB = 'RECEIVE_JOB'
export const JOB_CANCELED = 'JOB_CANCELED'
export const CANCELLING_JOB = 'CANCELLING_JOB'

function requestJob() {
	return {
		type: REQUEST_JOB
	}
}

export class JobMonitorConfiguration {
	jobId
	title = 'Processing'
	okButtonLabel = 'Ok'
	handleOkButtonClick
	handleCancelButtonClick
	handleJobCompleted
	handleJobFailed
}

export function startJobMonitor(jobMonitorConfiguration) {
	return {
		type: START_JOB_MONITOR,
		jobMonitorConfiguration: Object.assign(new JobMonitorConfiguration(), jobMonitorConfiguration)
	}
}

export function fetchJob(jobId) {
	return function (dispatch) {
		dispatch(requestJob())
		ServiceFactory.jobService.fetch(jobId).then(job => dispatch(receiveJob(job)))
	}
}

export function receiveJob(job) {
	return {
	    type: RECEIVE_JOB,
	    job: job,
	    receivedAt: Date.now()
	}
}

export function cancelJob(jobId) {
	return function (dispatch) {
		dispatch(cancellingJob())
		ServiceFactory.jobService.cancel(jobId).then(() => dispatch(jobCanceled(jobId)))
	}
}

function cancellingJob(jobId) {
	return {
		type: CANCELLING_JOB,
		jobId: jobId,
		receivedAt: Date.now()
	}
}

function jobCanceled(jobId) {
	return {
		type: JOB_CANCELED,
		jobId: jobId,
		receivedAt: Date.now()
	}
}

export function closeJobMonitor() {
	return {
		type: CLOSE_JOB_MONITOR
	}
}