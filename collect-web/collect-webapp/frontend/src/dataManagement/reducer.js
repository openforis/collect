import { RECEIVE_FULL_PREFERRED_SURVEY } from "../actions/index"
import { RECORD_DATA_TABLE_STATE_UPDATE } from "./recordDataTable/actions"
import { defaultDataManagementState } from "./state";
import { updateRecordDataTableState } from "./recordDataTable/state";

export default function dataManagement(
    state = defaultDataManagementState,
    action
) {
    const {type, ...otherProps} = action
    switch (type) {
        case RECEIVE_FULL_PREFERRED_SURVEY:
            return Object.assign(state, defaultDataManagementState)
        case RECORD_DATA_TABLE_STATE_UPDATE:
            return updateRecordDataTableState(state, {...otherProps})
        default:
            return state
    }
}