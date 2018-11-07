import ServiceFactory from 'services/ServiceFactory'

import {fetchRecordSummaries} from './recordDataTable/actions'

export const deleteRecords = (surveyId, loggedUserId, recordIds) =>
    dispatch => {
        ServiceFactory.recordService.delete(surveyId, loggedUserId, recordIds)
            .then(() => {
                dispatch(fetchRecordSummaries())
            })
    }