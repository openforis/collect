import {
    RECORD_DELETED
} from 'actions'


export default function records(
    state = {
        isFetching: false,
        list: null
    },
    action
) {
    switch (action.type) {
        case RECORD_DELETED:
            return Object.assign({}, state, {
                isFetching: true,
                list: null
            })
        default:
            return state
    }
}