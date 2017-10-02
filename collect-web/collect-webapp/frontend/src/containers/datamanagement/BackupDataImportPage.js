import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';
import Dropzone from 'react-dropzone';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

import ServiceFactory from 'services/ServiceFactory'
import JobMonitorModal from 'containers/job/JobMonitorModal'

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
            selectedRecordsToImport: null,
            selectedRecordsToImportIds: null,
            selectedConflictingRecords: null,
            selectedConflictingRecordsIds: null
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

    handleDataImportCompleteOkButtonClick() {
        this.setState({importStep: BackupDataImportPage.SELECT_PARAMETERS})
    }

    handleImportButtonClick() {

    }

    handleRecordsToImportRowSelect(row, isSelected, e) {
        let newSelectedRecordsToImport
        if (isSelected) {
            newSelectedRecordsToImport = this.state.selectedRecordsToImport.concat([row])
        } else {
            const oldArray = this.state.selectedRecordsToImport
            const idx = oldArray.indexOf(row)
            newSelectedRecordsToImport = oldArray.slice(0, idx).concat(oldArray.slice(idx + 1))
        }
        this.setState({selectedRecordsToImport: newSelectedRecordsToImport, 
            selectedRecordsToImportIds: newSelectedRecordsToImport.map(item => item.entryId)})
    }

    handleConflictingRecordsRowSelect(row, isSelected, e) {
        let newSelectedConflictingRecords
        if (isSelected) {
            newSelectedConflictingRecords = this.state.selectedConflictingRecords.concat([row])
        } else {
            const oldArray = this.state.selectedRecordsToImport
            const idx = oldArray.indexOf(row)
            newSelectedConflictingRecords = oldArray.slice(0, idx).concat(oldArray.slice(idx + 1))
        }
        this.setState({selectedConflictingRecords : newSelectedConflictingRecords, 
            selectedConflictingRecordsIds: newSelectedConflictingRecords.map(item => item.entryId)})
    }

    render() {
        const survey = this.props.survey
        if (survey == null) {
            return <div>Select a survey first</div>
        }
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
                let columns = []
                columns.push(<TableHeaderColumn dataField="entryId" isKey hidden row="0" rowSpan="2">Id</TableHeaderColumn>)
                
                const keyAttributes = survey.schema.firstRootEntityDefinition.keyAttributeDefinitions
                columns.push(<TableHeaderColumn row="0" colSpan={keyAttributes.length}>Keys</TableHeaderColumn>)
                const keyAttributeColumns = keyAttributes.map((keyAttr, i) => 
                    <TableHeaderColumn key={'key'+(i+1)} dataField={'key'+(i+1)} dataFormat={rootEntityKeyFormatter} 
                        width="80" row="1">{keyAttr.label}</TableHeaderColumn>)
                columns = columns.concat(keyAttributeColumns)
                
                function conflictingRecordRootEntityKeyFormatter(cell, row) {
                    var keyIdx = this.name.substring('conflictingKey'.length) - 1;
                    return row.conflictingRecord.rootEntityKeys[keyIdx]
                }
                let conflictingRecordsColumns = []
                conflictingRecordsColumns.push(<TableHeaderColumn dataField="entryId" isKey hidden row="0" rowSpan="2">Id</TableHeaderColumn>)
               
                conflictingRecordsColumns.push(<TableHeaderColumn row="0" colSpan={keyAttributes.length}>New Record</TableHeaderColumn>)
                conflictingRecordsColumns = conflictingRecordsColumns.concat(keyAttributeColumns)

                conflictingRecordsColumns.push(<TableHeaderColumn row="0" colSpan={keyAttributes.length}>Old Record</TableHeaderColumn>)
                
                const conflictingRecordKeyAttributeColumns = keyAttributes.map((keyAttr, i) => 
                    <TableHeaderColumn key={'conflictingKey'+(i+1)} dataField={'conflictingKey'+(i+1)} 
                        dataFormat={conflictingRecordRootEntityKeyFormatter} width="80" row="1">{keyAttr.label}</TableHeaderColumn>)
            
                conflictingRecordsColumns = conflictingRecordsColumns.concat(keyAttributeColumns)
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
                            open={this.state.jobStatusModalOpen}
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
