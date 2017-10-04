import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';
import Dropzone from 'react-dropzone';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

import * as Formatters from 'components/datatable/formatters'
import ServiceFactory from 'services/ServiceFactory'
import JobMonitorModal from 'containers/job/JobMonitorModal'
import Arrays from 'utils/Arrays'

class BackupDataImportPage extends Component {

    static SELECT_PARAMETERS = 'SELECT_PARAMETERS'
    static SHOW_IMPORT_SUMMARY = 'SHOW_IMPORT_SUMMARY'

    constructor(props) {
        super(props)
    
        this.state = {
            importStep: BackupDataImportPage.SELECT_PARAMETERS,
            fileSelected: false,
            summaryGenerationJobStatusModalOpen: false,
            dataImportJobStatusModalOpen: false,
            dataImportSummaryJobId: null,
            dataImportJobId: null,
            fileToBeImportedPreview: null,
            fileToBeImported: null,
            selectedRecordsToImport: [],
            selectedRecordsToImportIds: [],
            selectedConflictingRecords: [],
            selectedConflictingRecordsIds: []
        }
        this.onFileDrop = this.onFileDrop.bind(this)
        this.handleGenerateSummaryButtonClick = this.handleGenerateSummaryButtonClick.bind(this)
        this.handleRecordSummaryGenerationComplete = this.handleRecordSummaryGenerationComplete.bind(this)
        this.handleImportButtonClick = this.handleImportButtonClick.bind(this)
        this.handleDataImportCompleteOkButtonClick = this.handleDataImportCompleteOkButtonClick.bind(this)
        this.handleRecordsToImportRowSelect = this.handleRecordsToImportRowSelect.bind(this)
        this.handleConflictingRecordsRowSelect = this.handleConflictingRecordsRowSelect.bind(this)
    }

    handleGenerateSummaryButtonClick() {
        ServiceFactory.recordService.generateBackupDataImportSummary(this.props.survey, 
            this.props.survey.schema.firstRootEntityDefinition.name,
            this.state.fileToBeImported).then(job => {
                this.setState({summaryGenerationJobStatusModalOpen: true, dataImportSummaryJobId: job.id})
            })
    }

    onFileDrop(files) {
        const file = files[0]
        this.setState({fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name})
    }

    handleRecordSummaryGenerationComplete() {
        const $this = this
        ServiceFactory.recordService.loadBackupDataImportSummary(this.props.survey).then(summary => {
            $this.setState({summaryGenerationJobStatusModalOpen: false, 
                importStep: BackupDataImportPage.SHOW_IMPORT_SUMMARY, 
                dataImportSummary: summary,
                selectedRecordsToImport: summary.recordsToImport,
                selectedRecordsToImportIds: summary.recordsToImport.map(item => item.entryId)
            })
        })
    }

    handleRecordsToImportRowSelect(row, isSelected, e) {
        let newSelectedRecordsToImport = Arrays.addOrRemoveItem(this.state.selectedRecordsToImport, row, !isSelected)
        this.setState({selectedRecordsToImport: newSelectedRecordsToImport, 
            selectedRecordsToImportIds: newSelectedRecordsToImport.map(item => item.entryId)})
    }

    handleConflictingRecordsRowSelect(row, isSelected, e) {
        let newSelectedConflictingRecords = Arrays.addOrRemoveItem(this.state.selectedConflictingRecords, row, !isSelected)
        this.setState({selectedConflictingRecords : newSelectedConflictingRecords, 
            selectedConflictingRecordsIds: newSelectedConflictingRecords.map(item => item.entryId)})
    }

    handleImportButtonClick() {
        ServiceFactory.recordService.startBackupDataImportFromSummary(this.props.survey, 
            this.state.selectedRecordsToImportIds.concat(this.state.selectedConflictingRecordsIds),
            true).then(job => {
                this.setState({dataImportJobStatusModalOpen: true, dataImportJobId: job.id})
            })
    }

    handleDataImportCompleteOkButtonClick() {
        this.setState({dataImportJobStatusModalOpen: false, importStep: BackupDataImportPage.SELECT_PARAMETERS})
    }

    render() {
        const survey = this.props.survey
        if (survey == null) {
            return <div>Select a survey first</div>
        }
        const steps = ['ENTRY', 'CLEANSING', 'ANALYSIS']

        switch(this.state.importStep) {
            case BackupDataImportPage.SELECT_PARAMETERS:
                return (
                <Form>
                    <FormGroup>
                        <FormGroup row>
                            <Label for="file">File:</Label>
                            <Col sm={10}>
                                <Dropzone onDrop={(files) => this.onFileDrop(files)}>
                                    <div>Drop the file here, or click to select a file to upload.</div>
                                </Dropzone>
                                <div>{this.state.fileToBeImportedPreview ? this.state.fileToBeImportedPreview: ''}</div>
                            </Col>
                        </FormGroup>
                    </FormGroup>
                    <Row>
                        <Col sm={{offset: 1, size: 4}} colSpan={4}>
                            <Button disabled={! this.state.fileSelected} onClick={this.handleGenerateSummaryButtonClick} className="btn btn-success">Generate Import Summary</Button>
                        </Col>
                    </Row>
                    <JobMonitorModal
                        open={this.state.summaryGenerationJobStatusModalOpen}
                        title="Generating data import summary"
                        jobId={this.state.dataImportSummaryJobId}
                        okButtonLabel={'Done'}
                        handleJobCompleted={this.handleRecordSummaryGenerationComplete}
                    />
                </Form>
                )
            case BackupDataImportPage.SHOW_IMPORT_SUMMARY:
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

                let columns = []
                columns.push(<TableHeaderColumn dataField="entryId" isKey hidden>Id</TableHeaderColumn>)
                
                const keyAttributes = survey.schema.firstRootEntityDefinition.keyAttributeDefinitions
                columns.push(<TableHeaderColumn row="0" colSpan={keyAttributes.length}>Keys</TableHeaderColumn>)
                const keyAttributeColumns = keyAttributes.map((keyAttr, i) => 
                    <TableHeaderColumn key={'key'+(i+1)} dataField={'key'+(i+1)} dataFormat={rootEntityKeyFormatter} 
                        width="80" row="1">{keyAttr.label}</TableHeaderColumn>)
                columns = columns.concat(keyAttributeColumns)
                
                columns.push(<TableHeaderColumn row="0" colSpan={3}>Steps</TableHeaderColumn>)
                const stepPresentColumns = steps.map(s => <TableHeaderColumn key={s} dataField={s.toLowerCase() + 'DataPresent'} 
                    dataFormat={Formatters.checkedIconFormatter} width="50" row="1">{s}</TableHeaderColumn>)
                columns = columns.concat(stepPresentColumns)
                
                columns.push(<TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateFormatter}
                    dataAlign="center" width="80" row="0" rowSpan="2">Created</TableHeaderColumn>)
                columns.push(<TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateFormatter}
                    dataAlign="center" width="80" row="0" rowSpan="2">Modified</TableHeaderColumn>)
                
                function conflictingRecordRootEntityKeyFormatter(cell, row) {
                    var keyIdx = this.name.substring('conflictingKey'.length) - 1;
                    return row.conflictingRecord.rootEntityKeys[keyIdx]
                }

                let conflictingRecordsColumns = []
                conflictingRecordsColumns.push(<TableHeaderColumn dataField="entryId" isKey hidden>Id</TableHeaderColumn>)
               
                conflictingRecordsColumns.push(<TableHeaderColumn row="0" colSpan={keyAttributes.length}>Keys</TableHeaderColumn>)
                conflictingRecordsColumns = conflictingRecordsColumns.concat(keyAttributeColumns)
                
                conflictingRecordsColumns.push(<TableHeaderColumn row="0" colSpan={2 + steps.length}>New Record</TableHeaderColumn>)
                conflictingRecordsColumns.push(<TableHeaderColumn key="recordCreationDate" dataField="recordCreationDate" dataFormat={Formatters.dateFormatter}
                    dataAlign="center" width="80" row="1">Created</TableHeaderColumn>)
                conflictingRecordsColumns.push(<TableHeaderColumn key="recordModifiedDate" dataField="recordModifiedDate" dataFormat={Formatters.dateFormatter}
                    dataAlign="center" width="80" row="1">Modified</TableHeaderColumn>)
                conflictingRecordsColumns = conflictingRecordsColumns.concat(stepPresentColumns)

                conflictingRecordsColumns.push(<TableHeaderColumn row="0" colSpan={3}>Old Record</TableHeaderColumn>)

                conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordCreationDate" dataField="conflictingRecordCreationDate" dataFormat={Formatters.dateFormatter}
                    dataAlign="center" width="80" row="1">Created</TableHeaderColumn>)
                conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordModifiedDate" dataField="conflictingRecordModifiedDate" dataFormat={Formatters.dateFormatter}
                    dataAlign="center" width="80" row="1">Modified</TableHeaderColumn>)
                conflictingRecordsColumns.push(<TableHeaderColumn key="conflictingRecordStep" dataField="conflictingRecordStep"
                    dataAlign="center" width="80" row="1">Step</TableHeaderColumn>)
            
                
                conflictingRecordsColumns.push(<TableHeaderColumn key="importabilityLevel" dataField="importabilityLevel" dataFormat={importabilityFormatter}
                    dataAlign="center" width="50" row="0" rowSpan="2">Importability</TableHeaderColumn>)
           


                
                return (
                    <Form>
                        <FormGroup tag="fieldset">
                            <legend>Data import summary</legend>

                            <fieldset className="secondary">
                                <legend>Records to be imported ({this.state.selectedRecordsToImportIds.length}/{this.state.dataImportSummary.recordsToImport.length})</legend>
                                <BootstrapTable
                                    data={this.state.dataImportSummary.recordsToImport}
                                    striped	hover condensed
                                    height='200px'
                                    selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', 
                                        onSelect: this.handleRecordsToImportRowSelect, 
                                        selected: this.state.selectedRecordsToImportIds} }
                                    >
                                    {columns}
                                </BootstrapTable>
                            </fieldset>
                            <fieldset className="secondary">
                                <legend>Conflicting records</legend>
                                <BootstrapTable
                                    data={this.state.dataImportSummary.conflictingRecords}
                                    striped	hover condensed
                                    height='200px'
                                    selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', 
                                        onSelect: this.handleConflictingRecordsRowSelect, 
                                        selected: this.state.selectedConflictingRecordsIds} }
                                    >
                                    {conflictingRecordsColumns}
                                </BootstrapTable>
                            </fieldset>
                            <Row>
                                <Col sm={{offset: 1, size: 4}} colSpan={4}>
                                    <Button onClick={this.handleImportButtonClick} className="btn btn-success">Import</Button>
                                </Col>
                            </Row>
                        </FormGroup>
                        <JobMonitorModal
                            open={this.state.dataImportJobStatusModalOpen}
                            title="Importing data"
                            jobId={this.state.dataImportJobId}
                            okButtonLabel={'Done'}
                            handleOkButtonClick={this.handleDataImportCompleteOkButtonClick}
                        />
                    </Form>
                    )
        }
    }
}

const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(BackupDataImportPage);
