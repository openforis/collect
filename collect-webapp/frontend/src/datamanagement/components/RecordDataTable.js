import './RecordDataTable.scss'

import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { UncontrolledTooltip } from 'reactstrap'

import { DataGrid, DataGridValueFormatters } from 'common/components/DataGrid'
import OwnerColumnEditor from './OwnerColumnEditor'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'
import Strings from 'utils/Strings'
import { getDataManagementState } from 'datamanagement/state'
import {
  reloadRecordSummaries,
  sortRecordSummaries,
  changeRecordSummariesPage,
  filterRecordSummaries,
  filterOnlyOwnedRecords,
  updateRecordOwner,
} from 'datamanagement/recordDataTable/actions'
import { getRecordDataTableState } from 'datamanagement/recordDataTable/state'

const SUMMARY_FIELD_PREFIX = 'summary_'

const internalSortFieldByFieldKey = {
  key1: 'KEY1',
  key2: 'KEY2',
  key3: 'KEY3',
  [`${SUMMARY_FIELD_PREFIX}0`]: 'SUMMARY1',
  [`${SUMMARY_FIELD_PREFIX}1`]: 'SUMMARY2',
  [`${SUMMARY_FIELD_PREFIX}2`]: 'SUMMARY3',
  totalErrors: 'ERRORS',
  warnings: 'WARNINGS',
  owner: 'OWNER_NAME',
  step: 'STEP',
  entryComplete: 'STEP',
  cleansingComplete: 'STEP',
  creationDate: 'DATE_CREATED',
  modifiedDate: 'DATE_MODIFIED',
}

const fieldKeyByInternalSortField = {
  KEY1: 'key1',
  KEY2: 'key2',
  KEY3: 'key3',
  SUMMARY1: `${SUMMARY_FIELD_PREFIX}0`,
  SUMMARY2: `${SUMMARY_FIELD_PREFIX}1`,
  SUMMARY3: `${SUMMARY_FIELD_PREFIX}2`,
  ERRORS: 'totalErrors',
  WARNINGS: 'warnings',
  OWNER_NAME: 'owner',
  STEP: 'step',
  DATE_CREATED: 'creationDate',
  DATE_MODIFIED: 'modifiedDate',
}

const defaultSortModel = [{ field: 'modifiedDate', sort: 'desc' }]

class RecordDataTable extends Component {
  constructor(props) {
    super(props)
    this.onFilterModelChange = this.onFilterModelChange.bind(this)
  }

  static propTypes = {
    survey: PropTypes.object,
  }

  componentDidMount() {
    this.props.reloadRecordSummaries()
  }

  componentDidUpdate(prevProps) {
    const { surveyId } = this.props
    const { surveyId: prevSurveyId } = prevProps
    if (surveyId && (!prevSurveyId || surveyId !== prevSurveyId)) {
      this.props.reloadRecordSummaries()
    }
  }

  onFilterModelChange(filterModel) {
    const { filterRecordSummaries } = this.props

    const keyValues = []
    const summaryValues = []
    let ownerIds = []
    filterModel?.items?.forEach((filterItem) => {
      const { columnField, value } = filterItem
      if (columnField.startsWith('key')) {
        const keyValueIdx = parseInt(columnField.substring(3), 10) - 1
        keyValues[keyValueIdx] = Strings.appendIfMissing(value, '*') // starts with value
      } else if (columnField.startsWith(SUMMARY_FIELD_PREFIX)) {
        const summaryValueIdx = parseInt(columnField.substring(columnField.indexOf('_') + 1), 10)
        summaryValues[summaryValueIdx] = Strings.appendIfMissing(value, '*') // starts with value
      } else if (columnField === 'owner') {
        ownerIds = value
      }
    })
    filterRecordSummaries({ keyValues, summaryValues, ownerIds })
  }

  render() {
    const {
      surveyId,
      loggedUser,
      loading,
      records,
      totalSize,
      recordsPerPage,
      keyAttributes,
      attributeDefsShownInSummaryList,
      userCanChangeRecordOwner,
      roleInSurvey,
      selectedItemIds,
      sortFields,
      handleRowDoubleClick,
    } = this.props

    if (surveyId === null) {
      return <div>Please select a survey first</div>
    }

    const rootEntityKeyFormatter = ({ row, field }) => {
      const idx = field.substring(3) - 1
      return row.rootEntityKeys[idx]
    }

    const shownInSummaryListFormatter = ({ row, field }) => {
      const idx = field.substring(field.indexOf('_') + 1)
      return row.summaryValues[idx]
    }

    const renderCellOwner = ({ value: owner }) => {
      if (!owner) {
        return ''
      }
      if (!userCanChangeRecordOwner) {
        return owner.username
      }
      return (
        <span>
          <i className="fa fa-edit" aria-hidden="true"></i>
          &nbsp;
          {owner.username}
        </span>
      )
    }

    const renderCellLockedBy = ({ value, row }) => {
      if (!value) {
        return ''
      }
      const iconClass = value === loggedUser.username || loggedUser.canUnlockRecords() ? 'circle-orange' : 'circle-red'

      const iconId = `record-table-${row.id}-locked-by-icon`
      return (
        <span>
          <span className={iconClass} id={iconId}></span>
          <UncontrolledTooltip placement="top" target={iconId}>
            {L.l('dataManagement.recordLockedBy', value)}
          </UncontrolledTooltip>
        </span>
      )
    }

    const onPageChange = (page) => {
      this.props.changeRecordSummariesPage({ currentPage: page, recordsPerPage })
    }

    const onPageSizeChange = (pageSize) => {
      this.props.changeRecordSummariesPage({ currentPage: 0, recordsPerPage: pageSize })
    }

    const onSelectedIdsChange = (selectedIds) => {
      const selectedItems = selectedIds.map((selectedId) =>
        this.props.records.find((record) => record.id === selectedId)
      )
      this.props.handleItemsSelection(selectedItems)
    }

    const sortModel = sortFields.map((sortField) => {
      const fieldKey = fieldKeyByInternalSortField[sortField.field]
      if (!fieldKey) return null
      return {
        field: fieldKey,
        sort: sortField.descending ? 'desc' : 'asc',
      }
    })

    const onSortModelChange = (newSortModel) => {
      if (JSON.stringify(newSortModel) === JSON.stringify(sortModel)) {
        // sort unchanged
        return
      }
      const { sortRecordSummaries } = this.props
      const [{ field, sort }] = Arrays.isEmpty(newSortModel) ? defaultSortModel : newSortModel
      const sortField = internalSortFieldByFieldKey[field]
      if (!sortField) {
        console.log('unsupported sort field: ' + field)
        return
      }
      sortRecordSummaries([{ field: sortField, descending: sort === 'desc' }])
    }

    const keyAttributeColumns = keyAttributes.map((keyAttr, i) => ({
      field: `key${i + 1}`,
      valueFormatter: rootEntityKeyFormatter,
      flex: 1,
      sortable: true,
      headerName: keyAttr.labelOrName,
      quickSearch: true,
    }))

    const summaryAttributeColumns = attributeDefsShownInSummaryList.map((attr, i) => {
      const fieldKey = SUMMARY_FIELD_PREFIX + i
      const canFilterOrSort = loggedUser.canFilterRecordsBySummaryAttribute(attr, roleInSurvey)
      return {
        field: fieldKey,
        valueFormatter: shownInSummaryListFormatter,
        flex: 1,
        headerName: attr.labelOrName,
        quickSearch: canFilterOrSort,
      }
    })

    return (
      <DataGrid
        checkboxSelection
        className="records-data-grid"
        columns={[
          ...keyAttributeColumns,
          ...summaryAttributeColumns,
          { field: 'totalErrors', align: 'right', width: 100, sortable: true, headerName: 'dataManagement.errors' },
          { field: 'warnings', align: 'right', width: 120, sortable: true, headerName: 'dataManagement.warnings' },
          {
            field: 'creationDate',
            valueFormatter: DataGridValueFormatters.dateTime,
            width: 140,
            align: 'center',
            sortable: true,
            headerName: 'dataManagement.created',
          },
          {
            field: 'modifiedDate',
            valueFormatter: DataGridValueFormatters.dateTime,
            width: 140,
            align: 'center',
            sortable: true,
            headerName: 'dataManagement.modified',
          },
          {
            field: 'step',
            align: 'center',
            width: 120,
            sortable: true,
            headerName: 'dataManagement.workflow.step.label',
          },
          {
            field: 'owner',
            width: 150,
            sortable: true,
            headerName: 'dataManagement.owner',
            renderCell: renderCellOwner,
            editable: userCanChangeRecordOwner,
            renderEditCell: ({ api, field, id, row }) => (
              <OwnerColumnEditor
                owner={row.owner}
                users={this.props.users}
                onUpdate={({ owner }) => {
                  this.props.updateRecordOwner(row, owner)
                  // close cell editor
                  api.setCellMode(id, field, 'view')
                }}
              />
            ),
          },
          {
            field: 'lockedBy',
            renderCell: renderCellLockedBy,
            align: 'center',
            width: 60,
            sortable: true,
            renderHeader: () => (
              <i className="fa fa-lock" aria-hidden="true" title={L.l('dataManagement.recordLocked')} />
            ),
          },
        ]}
        dataMode="server"
        loading={loading}
        onFilterModelChange={this.onFilterModelChange}
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
        onRowDoubleClick={handleRowDoubleClick}
        onSelectedIdsChange={onSelectedIdsChange}
        onSortModelChange={onSortModelChange}
        pageSize={recordsPerPage}
        rowCount={totalSize}
        rows={records}
        selectionModel={selectedItemIds}
        sortModel={sortModel}
      />
    )
  }
}

const mapStateToProps = (state) => {
  const dataManagementState = getDataManagementState(state)
  const recordDataTableState = getRecordDataTableState(dataManagementState)

  const survey = state.activeSurvey ? state.activeSurvey.survey : null
  const users = state.users ? state.users.users : null
  const loggedUser = state.session ? state.session.loggedUser : null

  const roleInSurvey = survey.userInGroupRole

  const userCanChangeRecordOwner = loggedUser.canChangeRecordOwner(survey.userInGroupRole)

  const rootEntityDef = survey.schema.firstRootEntityDefinition
  const keyAttributes = rootEntityDef.keyAttributeDefinitions
  const attributeDefsShownInSummaryList = rootEntityDef.attributeDefinitionsShownInRecordSummaryList

  const {
    loading,
    currentPage,
    records,
    totalSize,
    recordsPerPage,
    keyValues,
    summaryValues,
    availableOwners,
    sortFields,
  } = recordDataTableState

  return {
    surveyId: survey ? survey.id : null,
    users,
    loggedUser,
    loading,
    currentPage,
    records,
    totalSize,
    recordsPerPage,
    keyAttributes,
    keyValues,
    attributeDefsShownInSummaryList,
    summaryValues,
    availableOwners,
    userCanChangeRecordOwner,
    roleInSurvey,
    sortFields,
  }
}

export default connect(mapStateToProps, {
  reloadRecordSummaries,
  sortRecordSummaries,
  changeRecordSummariesPage,
  filterRecordSummaries,
  filterOnlyOwnedRecords,
  updateRecordOwner,
})(RecordDataTable)
