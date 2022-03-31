import ServiceFactory from 'services/ServiceFactory'

import {
  getRecordDataTableState,
  getRecordDataTableCurrentPage,
  getRecordDataTableOwnerIds,
  getRecordDataTableKeyValues,
  getRecordDataTableRecords,
  getRecordDataTableRecordsPerPage,
  getRecordDataTableShowOnlyMyOwnRecords,
  getRecordDataTableSortFields,
  getRecordDataTableSummaryValues,
} from './state'
import { getDataManagementState } from '../state'

export const RESET_RECORD_DATA_TABLE_STATE = 'RESET_RECORD_DATA_TABLE_STATE'
export const RECORD_DATA_TABLE_STATE_UPDATE = 'RECORD_DATA_TABLE_STATE_UPDATE'
export const REQUEST_RECORD_SUMMARIES = 'REQUEST_RECORD_SUMMARIES'
export const RECEIVE_RECORD_SUMMARIES = 'RECEIVE_RECORD_SUMMARIES'
export const INVALIDATE_RECORD_SUMMARIES = 'INVALIDATE_RECORD_SUMMARIES'

const dispatchRecordDataTableStateUpdate = (dispatch, newState) =>
  dispatch({ type: RECORD_DATA_TABLE_STATE_UPDATE, ...newState })

const dispatchRecordDataTableLoadingStateUpdate = (dispatch, newState) =>
  dispatch({ type: RECORD_DATA_TABLE_STATE_UPDATE, loading: true, records: [], ...newState })

export const sortRecordSummaries = (sortFields) => (dispatch) => {
  dispatchRecordDataTableLoadingStateUpdate(dispatch, { sortFields })
  dispatch(fetchRecordSummaries())
}

export const changeRecordSummariesPage =
  ({ currentPage, recordsPerPage }) =>
  (dispatch) => {
    dispatchRecordDataTableLoadingStateUpdate(dispatch, { currentPage, recordsPerPage })
    dispatch(fetchRecordSummaries())
  }

export const filterRecordSummaries =
  ({ keyValues, summaryValues, ownerIds }) =>
  (dispatch) => {
    dispatchRecordDataTableLoadingStateUpdate(dispatch, { keyValues, summaryValues, ownerIds })
    dispatch(fetchRecordSummaries())
  }

export const filterRecordSummariesByOwners =
  ({ ownerIds }) =>
  (dispatch) => {
    dispatchRecordDataTableLoadingStateUpdate(dispatch, { ownerIds })
    dispatch(fetchRecordSummaries())
  }

export const filterOnlyOwnedRecords = (onlyOwnedRecords) => (dispatch, getState) => {
  const dataManagementState = getDataManagementState(getState())
  const recordDataTableState = getRecordDataTableState(dataManagementState)
  const ownerIds = onlyOwnedRecords ? [] : getRecordDataTableOwnerIds(recordDataTableState)
  dispatchRecordDataTableLoadingStateUpdate(dispatch, { ownerIds })
  dispatch(fetchRecordSummaries())
}

export const reloadRecordSummaries = () => (dispatch) => {
  dispatch({ type: RESET_RECORD_DATA_TABLE_STATE })
  dispatch(fetchRecordSummaries())
}

export const fetchRecordSummaries = () => (dispatch, getState) => {
  const state = getState()
  const survey = state.activeSurvey.survey
  const surveyId = survey.id
  const userId = state.session.loggedUser.id

  const dataManagementState = getDataManagementState(state)
  const recordDataTableState = getRecordDataTableState(dataManagementState)
  const page = getRecordDataTableCurrentPage(recordDataTableState)
  const recordsPerPage = getRecordDataTableRecordsPerPage(recordDataTableState)
  const onlyMyOwnRecords = getRecordDataTableShowOnlyMyOwnRecords(recordDataTableState)

  const keyValues = getRecordDataTableKeyValues(recordDataTableState)
  const summaryValues = getRecordDataTableSummaryValues(recordDataTableState)
  const ownerIds = onlyMyOwnRecords ? [userId] : getRecordDataTableOwnerIds(recordDataTableState)

  const sortFields = getRecordDataTableSortFields(recordDataTableState)

  const rootEntityName = survey.schema.firstRootEntityDefinition.name

  ServiceFactory.recordService
    .fetchRecordSummaries({
      surveyId,
      rootEntityName,
      userId,
      filterOptions: {
        recordsPerPage,
        page,
        keyValues,
        summaryValues,
        ownerIds,
      },
      sortFields,
    })
    .then((res) => {
      dispatchRecordDataTableStateUpdate(dispatch, {
        loading: false,
        records: res.records,
        totalSize: res.count,
        availableOwners: res.owners,
      })
    })
}

export const updateRecordOwner = (recordSummary, owner) => (dispatch, getState) => {
  const dataManagementState = getDataManagementState(getState())
  const recordDataTableState = getRecordDataTableState(dataManagementState)
  const records = getRecordDataTableRecords(recordDataTableState)
  const newRecords = records.map((r) => (r.id === recordSummary.id ? { ...r, owner } : r))
  dispatchRecordDataTableStateUpdate(dispatch, { records: newRecords })

  ServiceFactory.recordService.updateOwner(recordSummary, owner)
}
