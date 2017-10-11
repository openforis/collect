import {
    START_JOB_MONITOR,
    CLOSE_JOB_MONITOR,
    REQUEST_JOB,
    RECEIVE_JOB,
    JOB_CANCELED    
} from '../actions'

const initialState = {
    open: false,
    jobId: null,
    okButtonLabel: 'Ok',
    handleOkButtonClick: null,
    handleCancelButtonClick: null,
    handleJobCompleted: null
}

function currentJob(state = initialState, action) {
    switch (action.type) {
        case START_JOB_MONITOR:
            return { ...action, open: true }
        case CLOSE_JOB_MONITOR:
            return { open: false }
        case RECEIVE_JOB:
            if (action.job.completed && state.handleJobCompleted) {
                state.handleJobCompleted(action.job)
            }
            return { ...state, job: action.job}
        case JOB_CANCELED:
            return {...state, open: false}
        case REQUEST_JOB:
        default:
            return state
    }
}

export default currentJob