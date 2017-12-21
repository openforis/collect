import React, { Component } from 'react';
import { FormGroup, UncontrolledTooltip } from 'reactstrap';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';    
import * as Formatters from 'components/datatable/formatters'
import Forms from 'components/Forms'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

export default class BackupDataImportSummaryForm extends Component {

    render() {
        const survey = this.props.survey
        const dataImportSummary = this.props.dataImportSummary

        const steps = ['ENTRY', 'CLEANSING', 'ANALYSIS']
        
        function rootEntityKeyFormatter(cell, row) {
            var keyIdx = this.name.substring(3) - 1
            return row.record.rootEntityKeys[keyIdx]
        }
        function importabilityFormatter(cell, row) {
            let iconClass
            switch(cell) {
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

        const errorsFormatter = function(cell, row) {
            return (
                <div>
                    <span className="circle-red"  />
                </div>
            )
        }

        const errorMessagesFormatter = function(cell, row) {
            const errorItems = row.errors.map(e => {
                let message = e.message
                if (Strings.isNotBlank(e.path)) {
                    message += ' ' + L.l('dataManagement.backupDataImport.errors.path') + ': ' + e.path
                }
                return <li>{message}</li>
            })
            return <ul>{errorItems}</ul>
        }

        let columns = []
        columns.push(<TableHeaderColumn dataField="entryId" key="entryId" isKey hidden>Id</TableHeaderColumn>)
        
        const keyAttributes = survey.schema.firstRootEntityDefinition.keyAttributeDefinitions
        columns.push(<TableHeaderColumn row="0" key="keys" colSpan={keyAttributes.length}>{L.l('dataManagement.backupDataImport.keys')}</TableHeaderColumn>)
        const keyAttributeColumns = keyAttributes.map((keyAttr, i) => 
            <TableHeaderColumn key={'key'+(i+1)} dataField={'key'+(i+1)} dataFormat={rootEntityKeyFormatter} 
                width="80" row="1" dataSort>{keyAttr.label}</TableHeaderColumn>)
        columns = columns.concat(keyAttributeColumns)
        
        columns.push(<TableHeaderColumn key="steps" row="0" colSpan={3}>{L.l('dataManagement.backupDataImport.steps')}</TableHeaderColumn>)
        const stepPresentColumns = steps.map(s => <TableHeaderColumn key={s} dataField={s.toLowerCase() + 'DataPresent'} 
            dataFormat={Formatters.checkedIconFormatter} width="50" row="1">{s}</TableHeaderColumn>)
        columns = columns.concat(stepPresentColumns)
        
        columns.push(<TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="70" row="0" rowSpan="2" >{L.l('dataManagement.backupDataImport.createdOn')}</TableHeaderColumn>)
        columns.push(<TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="70" row="0" rowSpan="2">{L.l('dataManagement.backupDataImport.modifiedOn')}</TableHeaderColumn>)
        
        let conflictingRecordsColumns = []
        conflictingRecordsColumns.push(<TableHeaderColumn key="entryId" dataField="entryId" isKey hidden>Id</TableHeaderColumn>)
        
        conflictingRecordsColumns.push(<TableHeaderColumn key="keys" row="0" colSpan={keyAttributes.length}>{L.l('dataManagement.backupDataImport.keys')}</TableHeaderColumn>)
        conflictingRecordsColumns = conflictingRecordsColumns.concat(keyAttributeColumns)
        
        conflictingRecordsColumns.push(<TableHeaderColumn key="newRecord" row="0" colSpan={2 + steps.length}>{L.l('dataManagement.backupDataImport.newRecord')}</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.createdOn')}</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.modifiedOn')}</TableHeaderColumn>)
        conflictingRecordsColumns = conflictingRecordsColumns.concat(stepPresentColumns)

        conflictingRecordsColumns.push(<TableHeaderColumn key="oldRecord" row="0" colSpan={3}>{L.l('dataManagement.backupDataImport.oldRecord')}</TableHeaderColumn>)

        conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordCreationDate" dataField="conflictingRecordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.createdOn')}</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordModifiedDate" dataField="conflictingRecordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">{L.l('dataManagement.backupDataImport.modifiedOn')}</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordStep" dataField="conflictingRecordStep"
            dataAlign="center" width="80" row="1">Step</TableHeaderColumn>)

        conflictingRecordsColumns.push(<TableHeaderColumn key="importabilityLevel" dataField="importabilityLevel" dataFormat={importabilityFormatter}
            dataSort dataAlign="center" width="50" row="0" rowSpan="2">{L.l('dataManagement.backupDataImport.importability')}</TableHeaderColumn>)
        
        return (
            <FormGroup tag="fieldset">
                <legend>{L.l('dataManagement.backupDataImport.dataImportSummary')}</legend>

                {dataImportSummary.recordsToImport.length > 0 ? 
                    <fieldset className="secondary">
                        <legend>{L.l('dataManagement.backupDataImport.newRecordsToBeImported', [this.props.selectedRecordsToImportIds.length, dataImportSummary.recordsToImport.length])}</legend>
                        <BootstrapTable
                            data={dataImportSummary.recordsToImport}
                            striped	hover condensed
                            height='400px'
                            selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', 
                                onSelect: this.props.handleRecordsToImportRowSelect, 
                                onSelectAll: this.props.handleAllRecordsToImportSelect,
                                selected: this.props.selectedRecordsToImportIds} }
                            >
                            {columns}
                        </BootstrapTable>
                    </fieldset>
                : ''}
                {dataImportSummary.conflictingRecords.length > 0 ?
                    <fieldset className="secondary">
                        <legend>{L.l('dataManagement.backupDataImport.conflictingRecordsToBeImported', [this.props.selectedConflictingRecordsIds.length, dataImportSummary.conflictingRecords.length])}</legend>
                        <BootstrapTable
                            data={dataImportSummary.conflictingRecords}
                            striped	hover condensed
                            height='400px'
                            selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', 
                                onSelect: this.props.handleConflictingRecordsRowSelect, 
                                selected: this.props.selectedConflictingRecordsIds} }
                            >
                            {conflictingRecordsColumns}
                        </BootstrapTable>
                    </fieldset>
                : ''}
                {dataImportSummary.skippedFileErrors.length > 0 ?
                    <fieldset className="secondary">
                        <legend>{L.l('dataManagement.backupDataImport.errorsFound', [dataImportSummary.skippedFileErrors.length])}</legend>
                        <BootstrapTable
                            data={dataImportSummary.skippedFileErrors}
                            striped	hover condensed
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
}