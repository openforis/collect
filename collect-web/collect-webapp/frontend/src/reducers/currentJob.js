import {
    START_JOB_MONITOR,
    CLOSE_JOB_MONITOR,
    REQUEST_JOB,
    RECEIVE_JOB,
    JOB_CANCELED,
    CANCELLING_JOB
} from '../actions/job'

const initialState = {
    open: false,
    jobMonitorConfiguration: null,
    job: null,
    cancellingJob: false
}

function currentJob(state = initialState, action) {
    switch (action.type) {
        case START_JOB_MONITOR:
            return { ...action, open: true }
        case RECEIVE_JOB:
            switch(action.job.status) {
                case 'COMPLETED':
                    if (state.jobMonitorConfiguration.handleJobCompleted) {
                        state.jobMonitorConfiguration.handleJobCompleted(action.job)
                    }
                    break
                case 'FAILED':
                    if (state.jobMonitorConfiguration.handleJobFailed) {
                        state.jobMonitorConfiguration.handleJobFailed(action.job)
                    }
                    break
            }
            return { ...state, 
                job: action.job
            }
        case CANCELLING_JOB:
            return { ...state, 
                cancellingJob: true
            }
        case JOB_CANCELED:
            return initialState
        case CLOSE_JOB_MONITOR:
            return initialState
        default:
            return state
    }
}

export default currentJob