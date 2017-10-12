import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';
import Dropzone from 'react-dropzone';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

import * as Formatters from 'components/datatable/formatters'
import BackupDataImportSummaryForm from 'components/datamanagement/BackupDataImportSummaryForm'
import ServiceFactory from 'services/ServiceFactory'
import JobMonitorModal from 'components/JobMonitorModal'
import Arrays from 'utils/Arrays'
import * as Actions from 'actions';

class BackupDataImportPage extends Component {

    static SELECT_PARAMETERS = 'SELECT_PARAMETERS'
    static SHOW_IMPORT_SUMMARY = 'SHOW_IMPORT_SUMMARY'

    constructor(props) {
        super(props)
    
        this.state = {
            importStep: BackupDataImportPage.SELECT_PARAMETERS,
            summaryGenerationJobStatusModalOpen: false,
            dataImportJobStatusModalOpen: false,
            dataImportSummaryJobId: null,
            dataImportJobId: null,
            fileSelected: false,
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
        const survey = this.props.survey
        ServiceFactory.recordService.generateBackupDataImportSummary(survey, 
            survey.schema.firstRootEntityDefinition.name,
            this.state.fileToBeImported)
        .then(job => {
            this.props.dispatch(Actions.startJobMonitor({
                jobId: job.id, 
                title: 'Generating data import summary',
                okButtonLabel: 'Done',                        
                handleJobCompleted: this.handleRecordSummaryGenerationComplete
            }))
        })
    }

    onFileDrop(files) {
        const file = files[0]
        this.setState({fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name})
    }

    handleRecordSummaryGenerationComplete() {
        const $this = this
        ServiceFactory.recordService.loadBackupDataImportSummary(this.props.survey).then(summary => {
            this.props.dispatch(Actions.closeJobMonitor())
            $this.setState({ 
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
        this.props.history.push('/datamanagement/')
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
                    <FormGroup row>
                        <Label for="file">File:</Label>
                        <Col sm={10}>
                            <Dropzone accept=".collect-backup,.collect-data,.zip" onDrop={(files) => this.onFileDrop(files)} style={{
                                width: '100%', height: '200px', 
                                borderWidth: '2px', borderColor: 'rgb(102, 102, 102)', 
                                borderStyle: 'dashed', borderRadius: '5px'
                            }}>
                            {this.state.fileToBeImportedPreview ?
                                <p style={{fontSize: '2em', textAlign: 'center'}}><span className="checked large" />{this.state.fileToBeImportedPreview}</p>
                                : <p>Click to select a .collect-backup or .collect-data file or drop it here.</p>
                            }
                            </Dropzone>
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Col sm={{offset: 5, size: 2}} colSpan={2}>
                            <Button disabled={! this.state.fileSelected} onClick={this.handleGenerateSummaryButtonClick} className="btn btn-success">Generate Import Summary</Button>
                        </Col>
                    </FormGroup>
                    
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
                return (
                    <FormGroup>
                        <BackupDataImportSummaryForm 
                            survey={survey} 
                            dataImportSummary={this.state.dataImportSummary}
                            selectedRecordsToImportIds={this.state.selectedRecordsToImportIds}
                            handleRecordsToImportRowSelect={this.handleRecordsToImportRowSelect}
                            handleConflictingRecordsRowSelect={this.handleConflictingRecordsRowSelect}
                            selectedConflictingRecordsIds={this.state.selectedConflictingRecordsIds}
                            />
                        <Row>
                            <Col sm={{offset: 5, size: 2}} colSpan={2}>
                                <Button onClick={this.handleImportButtonClick} className="btn btn-success">Import</Button>
                            </Col>
                        </Row>
                        <JobMonitorModal
                            open={this.state.dataImportJobStatusModalOpen}
                            title="Importing data"
                            jobId={this.state.dataImportJobId}
                            okButtonLabel={'Done'}
                            handleOkButtonClick={this.handleDataImportCompleteOkButtonClick}
                        />
                    </FormGroup>
                )
        }
    }
}

const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(BackupDataImportPage);
