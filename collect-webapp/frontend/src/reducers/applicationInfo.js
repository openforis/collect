import {
    REQUEST_APPLICATION_INFO, RECEIVE_APPLICATION_INFO
} from '../actions'

export default function applicationInfo(
    state = {
        isReady: false,
        isFetching: false,
        didInvalidate: false,
        lastUpdated: null,
        info: null
    },
    action
) {
    switch (action.type) {
        case REQUEST_APPLICATION_INFO:
            return Object.assign({}, state, {
                isReady: false,
                isFetching: true,
                didInvalidate: false
            })
        case RECEIVE_APPLICATION_INFO:
            return Object.assign({}, state, {
                isReady: true,
                isFetching: false,
                didInvalidate: false,
                info: action.info,
                lastUpdated: action.receivedAt
            })
        default:
            return state
    }
}
