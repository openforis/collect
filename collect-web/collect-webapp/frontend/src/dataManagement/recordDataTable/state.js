export const recordDataTableDefaultState = {
    currentPage: 1,
    recordsPerPage: 25,
    keyValues: [],
    summaryValues: [],
    sortFields: [{field: 'DATE_MODIFIED', descending: true}],
    records: [],
    totalSize: 0,
    availableOwners: [],
    onlyMyOwnRecords: false,
    ownerIds: [],
}

export const getRecordDataTableState = (dataManagementState) => dataManagementState.recordDataTable

export const getRecordDataTableCurrentPage = (dataManagementState) => 
    dataManagementState.currentPage || 1

export const setRecordDataTableCurrentPage = (dataManagementState, page) => 
    dataManagementState.currentPage = page

export const getRecordDataTableRecordsPerPage = (dataManagementState) => 
    dataManagementState.recordsPerPage || 25

export const setRecordDataTableRecordsPerPage = (dataManagementState, recordsPerPage) =>
    dataManagementState.recordsPerPage = recordsPerPage

export const getRecordDataTableRecords = (dataManagementState) =>
    dataManagementState.records
    
export const getRecordDataTableSortFields = (dataManagementState) => 
    dataManagementState.sortFields

export const getRecordDataTableShowOnlyMyOwnRecords = (dataManagementState) => 
    dataManagementState.onlyMyOwnRecords

export const getRecordDataTableKeyValues = (dataManagementState) => 
    dataManagementState.keyValues

export const getRecordDataTableSummaryValues = (dataManagementState) => 
    dataManagementState.keyValues

export const getRecordDataTableOwnerIds = (dataManagementState) => 
    dataManagementState.ownerIds

export const updateRecordDataTableState = (state, newProps) => {
    const recordDataTableState = getRecordDataTableState(state)
    const newRecordDataTableState = Object.assign(recordDataTableState, newProps)
    return Object.assign(state, {recordDataTable: newRecordDataTableState})
}