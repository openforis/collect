import {
    START_JOB_MONITOR,
    CLOSE_JOB_MONITOR
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
        default:
            return state
    }
}

export default currentJob