import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col, Tooltip } from 'reactstrap';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';    
import * as Formatters from 'components/datatable/formatters'

export default class BackupDataImportSummaryForm extends Component {

    constructor(props) {
        super(props)
    }

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
                    iconClass = "redCircle"
                    break
                case 0:
                    iconClass = "equalSign"
                    break
                case 1:
                    iconClass = "greenCircle"
                    break
            }
            return <span className={iconClass}></span>
        }

        const errorsFormatter = function(cell, row) {
            const errors = row.errors
            const errorMessage = errors.map(e => 'Error: ' + e.message + ' Path: ' + e.path)
                .join('<br>')
            return (
                <div style={{}}>
                    <span id={row.fileName + '_errorMessageIcon'} className="redCircle"  />
                    <Tooltip placement="right" target={row.fileName + '_errorMessageIcon'}>{errorMessage}</Tooltip>
                </div>
            )
        }

        let columns = []
        columns.push(<TableHeaderColumn dataField="entryId" key="entryId" isKey hidden>Id</TableHeaderColumn>)
        
        const keyAttributes = survey.schema.firstRootEntityDefinition.keyAttributeDefinitions
        columns.push(<TableHeaderColumn row="0" key="keys" colSpan={keyAttributes.length}>Keys</TableHeaderColumn>)
        const keyAttributeColumns = keyAttributes.map((keyAttr, i) => 
            <TableHeaderColumn key={'key'+(i+1)} dataField={'key'+(i+1)} dataFormat={rootEntityKeyFormatter} 
                width="80" row="1" dataSort>{keyAttr.label}</TableHeaderColumn>)
        columns = columns.concat(keyAttributeColumns)
        
        columns.push(<TableHeaderColumn key="steps" row="0" colSpan={3}>Steps</TableHeaderColumn>)
        const stepPresentColumns = steps.map(s => <TableHeaderColumn key={s} dataField={s.toLowerCase() + 'DataPresent'} 
            dataFormat={Formatters.checkedIconFormatter} width="50" row="1">{s}</TableHeaderColumn>)
        columns = columns.concat(stepPresentColumns)
        
        columns.push(<TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="0" rowSpan="2" >Created</TableHeaderColumn>)
        columns.push(<TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="0" rowSpan="2">Modified</TableHeaderColumn>)
        
        function conflictingRecordRootEntityKeyFormatter(cell, row) {
            var keyIdx = this.name.substring('conflictingKey'.length) - 1;
            return row.conflictingRecord.rootEntityKeys[keyIdx]
        }

        let conflictingRecordsColumns = []
        conflictingRecordsColumns.push(<TableHeaderColumn key="entryId" dataField="entryId" isKey hidden>Id</TableHeaderColumn>)
        
        conflictingRecordsColumns.push(<TableHeaderColumn key="keys" row="0" colSpan={keyAttributes.length}>Keys</TableHeaderColumn>)
        conflictingRecordsColumns = conflictingRecordsColumns.concat(keyAttributeColumns)
        
        conflictingRecordsColumns.push(<TableHeaderColumn key="newRecord" row="0" colSpan={2 + steps.length}>New Record</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">Created</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">Modified</TableHeaderColumn>)
        conflictingRecordsColumns = conflictingRecordsColumns.concat(stepPresentColumns)

        conflictingRecordsColumns.push(<TableHeaderColumn key="oldRecord" row="0" colSpan={3}>Old Record</TableHeaderColumn>)

        conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordCreationDate" dataField="conflictingRecordCreationDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">Created</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordModifiedDate" dataField="conflictingRecordModifiedDate" dataFormat={Formatters.dateTimeFormatter}
            dataSort dataAlign="center" width="80" row="1">Modified</TableHeaderColumn>)
        conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordStep" dataField="conflictingRecordStep"
            dataAlign="center" width="80" row="1">Step</TableHeaderColumn>)

        conflictingRecordsColumns.push(<TableHeaderColumn key="importabilityLevel" dataField="importabilityLevel" dataFormat={importabilityFormatter}
            dataSort dataAlign="center" width="50" row="0" rowSpan="2">Importability</TableHeaderColumn>)
        
        return (
            <FormGroup tag="fieldset">
                <legend>Data import summary</legend>

                {dataImportSummary.recordsToImport.length > 0 ? 
                    <fieldset className="secondary">
                        <legend>New records to be imported: {this.props.selectedRecordsToImportIds.length}/{dataImportSummary.recordsToImport.length}</legend>
                        <BootstrapTable
                            data={dataImportSummary.recordsToImport}
                            striped	hover condensed
                            height='400px'
                            selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', 
                                onSelect: this.props.handleRecordsToImportRowSelect, 
                                selected: this.props.selectedRecordsToImportIds} }
                            >
                            {columns}
                        </BootstrapTable>
                    </fieldset>
                : ''}
                {dataImportSummary.conflictingRecords.length > 0 ?
                    <fieldset className="secondary">
                        <legend>Conflicting records to be imported: {this.props.selectedConflictingRecordsIds.length}/{dataImportSummary.conflictingRecords.length}</legend>
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
                        <legend>Errors found ({dataImportSummary.skippedFileErrors.length})</legend>
                        <BootstrapTable
                            data={dataImportSummary.skippedFileErrors}
                            striped	hover condensed
                            height='300px'>
                            <TableHeaderColumn dataField="fileName" isKey width="200">File name</TableHeaderColumn>
                            <TableHeaderColumn dataField="errors" dataFormat={errorsFormatter} width="50">Errors</TableHeaderColumn>
                        </BootstrapTable>
                    </fieldset>
                : ''}
            </FormGroup>
        )
    }
}