export const recordDataTableDefaultState = {
    currentPage: 1,
    recordsPerPage: 25,
    keyValues: [],
    summaryValues: [],
    sortFields: [{ field: 'DATE_MODIFIED', descending: true }],
    records: [],
    totalSize: 0,
    availableOwners: [],
    onlyMyOwnRecords: false,
    ownerIds: [],
}

export const getRecordDataTableState = (dataManagementState) => dataManagementState.recordDataTable

export const getRecordDataTableCurrentPage = (state) =>
    state.currentPage || 1

export const setRecordDataTableCurrentPage = (state, page) =>
    state.currentPage = page

export const getRecordDataTableRecordsPerPage = (state) =>
    state.recordsPerPage || 25

export const setRecordDataTableRecordsPerPage = (state, recordsPerPage) =>
    state.recordsPerPage = recordsPerPage

export const getRecordDataTableRecords = (state) =>
    state.records

export const getRecordDataTableSortFields = (state) =>
    state.sortFields

export const getRecordDataTableShowOnlyMyOwnRecords = (state) =>
    state.onlyMyOwnRecords

export const getRecordDataTableKeyValues = (state) =>
    state.keyValues

export const getRecordDataTableSummaryValues = (state) =>
    state.summaryValues

export const getRecordDataTableOwnerIds = (state) =>
    state.ownerIds

export const updateRecordDataTableState = (state, newProps) => 
    ({...state, ...newProps})