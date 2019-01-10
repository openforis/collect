import ServiceFactory from 'services/ServiceFactory'

import { fetchRecordSummaries } from './recordDataTable/actions'

export const RECORD_LOCKED = 'RECORD_LOCKED'
export const RECORD_UNLOCKED = 'RECORD_UNLOCKED'

export const deleteRecords = (surveyId, loggedUserId, recordIds) =>
    dispatch => {
        ServiceFactory.recordService.delete(surveyId, loggedUserId, recordIds)
            .then(() => {
                dispatch(fetchRecordSummaries())
            })
    }

export const recordLocked = (recordId, lockedBy) => dispatch =>
    dispatch({ type: RECORD_LOCKED, recordId, lockedBy })

export const recordUnlocked = recordId => dispatch =>
    dispatch({ type: RECORD_UNLOCKED, recordId })

