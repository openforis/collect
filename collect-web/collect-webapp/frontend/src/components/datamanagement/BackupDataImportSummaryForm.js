import React from 'react';
import { FormGroup } from 'reactstrap';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';

import * as Formatters from 'components/datatable/formatters'
import L from 'utils/Labels'
import Strings from 'utils/Strings'
import Workflow from 'model/Workflow'

const stepColumns = Workflow.STEP_CODES.map(s => 
    <TableHeaderColumn key={s} dataField={s.toLowerCase() + 'DataPresent'}
        dataFormat={Formatters.checkedIconFormatter} width="50" row="1">{s}</TableHeaderColumn>
)

const createKeyAttributesColumns = survey => {
    const keyAttributes = survey.schema.firstRootEntityDefinition.keyAttributeDefinitions

    function rootEntityKeyFormatter(cell, row) {
        var keyIdx = this.name.substring(3) - 1
        return row.record.rootEntityKeys[keyIdx]
    }
    
    return keyAttributes.map((keyAttr, i) =>
            <TableHeaderColumn key={'key' + (i + 1)}
                dataField={'key' + (i + 1)}
                dataFormat={rootEntityKeyFormatter}
                width="80" row="1" dataSort>
                {keyAttr.label}
            </TableHeaderColumn>
        )
}

const NewRecordsTable = props => {
    const {
        survey,
        recordsToImport,
        selectedRecordsToImportIds,
        handleAllRecordsToImportSelect,
        handleRecordsToImportRowSelect,
    } = props

    const keyAttributeColumns = createKeyAttributesColumns(survey)
    const columns = [
        <TableHeaderColumn dataField="entryId" key="entryId" isKey hidden>Id</TableHeaderColumn>,
        //KEY ATTRIBUTES HEADER
        <TableHeaderColumn row="0" key="keys" colSpan={keyAttributeColumns.length}>
            {L.l('dataManagement.backupDataImport.keys')}</TableHeaderColumn>,
    ].concat(
        keyAttributeColumns
    ).concat([
        //STEPS COLUMNS HEADER
        <TableHeaderColumn key="steps" row="0" colSpan={3}>
            {L.l('dataManagement.backupDataImport.steps')}</TableHeaderColumn>
    ]).concat(stepColumns)
    .concat([
        <TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="70" row="0" rowSpan="2" >{L.l('dataManagement.backupDataImport.createdOn')}</TableHeaderColumn>,
        <TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="70" row="0" rowSpan="2">{L.l('dataManagement.backupDataImport.modifiedOn')}</TableHeaderColumn>,
    ])
    
    return <BootstrapTable
        data={recordsToImport}
        striped hover condensed
        height='400px'
        selectRow={{
            mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue',
            onSelect: handleRecordsToImportRowSelect,
            onSelectAll: handleAllRecordsToImportSelect,
            selected: selectedRecordsToImportIds
        }}
    >{columns}</BootstrapTable>
}

const ConflictingRecordsTable = props => {
    const {
        survey,
        conflictingRecords,
        selectedConflictingRecordsIds,
        handleConflictingRecordsRowSelect,
        handleAllConflictingRecordsSelect,
    } = props

    function importabilityFormatter(cell, row) {
        let iconClass
        switch (cell) {
            case -1:
                iconClass = "circle-red"
                break
            case 0:
                iconClass = "equal-sign"
                break
            case 1:
                iconClass = "circle-green"
                break
            default:
                iconClass = ""
        }
        return <span className={iconClass}></span>
    }

    const keyAttributeColumns = createKeyAttributesColumns(survey)

    const conflictingRecordsColumns = [
        <TableHeaderColumn key="entryId" dataField="entryId" isKey hidden>Id</TableHeaderColumn>,
        <TableHeaderColumn key="keys" row="0" colSpan={keyAttributeColumns.length}>{L.l('dataManagement.backupDataImport.keys')}</TableHeaderColumn>,        
    ].concat(
        keyAttributeColumns
    ).concat([
        <TableHeaderColumn key="newRecord" row="0" colSpan={2 + stepColumns.length}>{L.l('dataManagement.backupDataImport.newRecord')}</TableHeaderColumn>,
        <TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.createdOn')}</TableHeaderColumn>,
        <TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.modifiedOn')}</TableHeaderColumn>,
    ]).concat(stepColumns)
    .concat([
        <TableHeaderColumn key="oldRecord" row="0" colSpan={3}>{L.l('dataManagement.backupDataImport.oldRecord')}</TableHeaderColumn>,
        <TableHeaderColumn key="conflictingRecordCreationDate" dataField="conflictingRecordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.createdOn')}</TableHeaderColumn>,
        <TableHeaderColumn key="conflictingRecordModifiedDate" dataField="conflictingRecordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.modifiedOn')}</TableHeaderColumn>,
        <TableHeaderColumn key="conflictingRecordStep" dataField="conflictingRecordStep"
            dataAlign="center" width="80" row="1">Step</TableHeaderColumn>,
        <TableHeaderColumn key="importabilityLevel" dataField="importabilityLevel" dataFormat={importabilityFormatter}
            dataSort dataAlign="center" width="50" row="0" rowSpan="2">{L.l('dataManagement.backupDataImport.importability')}</TableHeaderColumn>
    ])
    
    return (
        <BootstrapTable
            data={conflictingRecords}
            striped hover condensed
            height='400px'
            selectRow={{
                mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue',
                onSelect: handleConflictingRecordsRowSelect,
                onSelectAll: handleAllConflictingRecordsSelect,
                selected: selectedConflictingRecordsIds
            }}
        >{conflictingRecordsColumns}</BootstrapTable>
    )
}

const BackupDataImportSummaryForm = props => {

    const {
        survey,
        dataImportSummary,
        selectedRecordsToImportIds,
        handleAllRecordsToImportSelect,
        handleRecordsToImportRowSelect,
        selectedConflictingRecordsIds,
        handleConflictingRecordsRowSelect,
        handleAllConflictingRecordsSelect,
    } = props

    const errorsFormatter = function (cell, row) {
        return (
            <div>
                <span className="circle-red" />
            </div>
        )
    }

    const errorMessagesFormatter = function (cell, row) {
        const errorItems = row.errors.map(e => {
            let message = e.message
            if (Strings.isNotBlank(e.path)) {
                message += ' ' + L.l('dataManagement.backupDataImport.errors.path') + ': ' + e.path
            }
            return <li>{message}</li>
        })
        return <ul>{errorItems}</ul>
    }

    return (
        <FormGroup tag="fieldset">
            <legend>{L.l('dataManagement.backupDataImport.dataImportSummary')}</legend>

            {dataImportSummary.recordsToImport.length > 0 ?
                <fieldset className="secondary">
                    <legend>{L.l('dataManagement.backupDataImport.newRecordsToBeImported', [selectedRecordsToImportIds.length, dataImportSummary.recordsToImport.length])}</legend>
                    <NewRecordsTable
                        survey={survey}
                        recordsToImport={dataImportSummary.recordsToImport}
                        selectedRecordsToImportIds={selectedRecordsToImportIds}
                        handleAllRecordsToImportSelect={handleAllRecordsToImportSelect}
                        handleRecordsToImportRowSelect={handleRecordsToImportRowSelect} />
                </fieldset>
                : ''}
            {dataImportSummary.conflictingRecords.length > 0 ?
                <fieldset className="secondary">
                    <legend>{L.l('dataManagement.backupDataImport.conflictingRecordsToBeImported', [selectedConflictingRecordsIds.length, dataImportSummary.conflictingRecords.length])}</legend>
                    <ConflictingRecordsTable
                        survey={survey}
                        conflictingRecords={dataImportSummary.conflictingRecords}
                        selectedConflictingRecordsIds={selectedConflictingRecordsIds}
                        handleConflictingRecordsRowSelect={handleConflictingRecordsRowSelect}
                        handleAllConflictingRecordsSelect={handleAllConflictingRecordsSelect}
                        />
                </fieldset>
                : ''}
            {dataImportSummary.skippedFileErrors.length > 0 ?
                <fieldset className="secondary">
                    <legend>{L.l('dataManagement.backupDataImport.errorsFound', [dataImportSummary.skippedFileErrors.length])}</legend>
                    <BootstrapTable
                        data={dataImportSummary.skippedFileErrors}
                        striped hover condensed
                        height='300px'>
                        <TableHeaderColumn dataField="fileName" isKey width="80">{L.l('dataManagement.backupDataImport.errors.fileName')}</TableHeaderColumn>
                        <TableHeaderColumn dataField="errors" dataFormat={errorsFormatter} width="40">{L.l('dataManagement.backupDataImport.errors.label')}</TableHeaderColumn>
                        <TableHeaderColumn dataField="errors" dataFormat={errorMessagesFormatter} width="600">{L.l('dataManagement.backupDataImport.errors.messages')}</TableHeaderColumn>
                    </BootstrapTable>
                </fieldset>
                : ''}
        </FormGroup>
    )
}

export default BackupDataImportSummaryForm