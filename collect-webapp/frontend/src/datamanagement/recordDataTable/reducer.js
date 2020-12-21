import { RECORD_DATA_TABLE_STATE_UPDATE, RESET_RECORD_DATA_TABLE_STATE } from './actions'
import { recordDataTableDefaultState, updateRecordDataTableState, updateRecordLockState } from './state'
import { RECEIVE_FULL_ACTIVE_SURVEY } from 'actions/activeSurvey'
import { RECORD_LOCKED, RECORD_UNLOCKED } from '../actions'

export default function recordDataTable(state = recordDataTableDefaultState, action) {
  const { type, ...otherProps } = action
  switch (type) {
    case RECORD_DATA_TABLE_STATE_UPDATE:
      return updateRecordDataTableState(state, { ...otherProps })
    case RECEIVE_FULL_ACTIVE_SURVEY:
    case RESET_RECORD_DATA_TABLE_STATE:
      return { ...recordDataTableDefaultState }
    case RECORD_LOCKED:
      return updateRecordLockState(state, action.recordId, action.lockedBy)
    case RECORD_UNLOCKED:
      return updateRecordLockState(state, action.recordId, null)
    default:
      return state
  }
}
