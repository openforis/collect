import React, { Component } from 'react';
import { Button, Form, FormGroup, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import Dropzone from 'components/Dropzone';
import BackupDataImportSummaryForm from 'components/datamanagement/BackupDataImportSummaryForm'
import ServiceFactory from 'services/ServiceFactory'
import Arrays from 'utils/Arrays'
import RouterUtils from 'utils/RouterUtils'
import L from 'utils/Labels';
import * as JobActions from 'actions/job';

class BackupDataImportPage extends Component {

    static SELECT_PARAMETERS = 'SELECT_PARAMETERS'
    static SHOW_IMPORT_SUMMARY = 'SHOW_IMPORT_SUMMARY'

    constructor(props) {
        super(props)
    
        this.state = {
            importStep: BackupDataImportPage.SELECT_PARAMETERS,
            fileSelected: false,
            fileToBeImportedPreview: null,
            fileToBeImported: null,
            uploadingFile: false,
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
        this.handleAllRecordsToImportSelect = this.handleAllRecordsToImportSelect.bind(this)
        this.handleConflictingRecordsRowSelect = this.handleConflictingRecordsRowSelect.bind(this)
    }

    handleGenerateSummaryButtonClick() {
        const survey = this.props.survey
        this.setState({
            uploadingFile: true
        })
        ServiceFactory.recordService.generateBackupDataImportSummary(
                survey, 
                survey.schema.firstRootEntityDefinition.name,
                this.state.fileToBeImported
            ).then(job => {
                this.setState({
                    uploadingFile: false
                })
                this.props.dispatch(JobActions.startJobMonitor({
                    jobId: job.id, 
                    title: L.l('dataManagement.backupDataImport.generatingDataImportSummary'),
                    okButtonLabel: L.l('global.done'),                        
                    handleJobCompleted: this.handleRecordSummaryGenerationComplete
                }))
        })
    }

    onFileDrop(file) {
        this.setState({fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name})
    }

    handleRecordSummaryGenerationComplete() {
        const $this = this
        ServiceFactory.recordService.loadBackupDataImportSummary(this.props.survey).then(summary => {
            this.props.dispatch(JobActions.closeJobMonitor())
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
        this.handleSelectedRecordsToImportChange(newSelectedRecordsToImport)
    }

    
    handleAllRecordsToImportSelect(isSelected, rows) {
        const newSelectedRecordsToImport = Arrays.addOrRemoveItems(this.state.selectedRecordsToImport, rows, !isSelected)
		this.handleSelectedRecordsToImportChange(newSelectedRecordsToImport)
    }

    handleSelectedRecordsToImportChange(newSelectedRecordsToImport) {
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
                this.props.dispatch(JobActions.startJobMonitor({
                    jobId: job.id, 
                    title: L.l('dataManagement.backupDataImport.importingData'),
                    okButtonLabel: L.l('global.done'),
                    handleOkButtonClick: this.handleDataImportCompleteOkButtonClick
                }))
            })
    }

    handleDataImportCompleteOkButtonClick() {
        RouterUtils.navigateToDataManagementHomePage(this.props.history)
    }

    render() {
        const survey = this.props.survey
        if (survey == null) {
            return <div>Select a survey first</div>
        }
        const acceptedFileTypesDescription = L.l('dataManagement.backupDataImport.acceptedFileTypesDescription')
        
        switch(this.state.importStep) {
            case BackupDataImportPage.SELECT_PARAMETERS:
                return (
                    <Form>
                        <FormGroup row>
                            <Col sm={12}>
                                <Dropzone 
                                    acceptedFileTypes={'.collect-backup,.collect-data,.zip'}
                                    acceptedFileTypesDescription={acceptedFileTypesDescription}
                                    handleFileDrop={this.onFileDrop}
                                    height="300px"
                                    fileToBeImportedPreview={this.state.fileToBeImportedPreview} />
                            </Col>
                        </FormGroup>
                        {this.state.fileSelected && 
                            <FormGroup row>
                                <Col sm={{offset: 5, size: 2}} colSpan={2}>
                                    <Button disabled={this.state.uploadingFile} onClick={this.handleGenerateSummaryButtonClick} 
                                        className="btn btn-success">{L.l('dataManagement.backupDataImport.generateImportSummary')}</Button>
                                </Col>
                            </FormGroup>
                        }
                        
                    </Form>
                )
            case BackupDataImportPage.SHOW_IMPORT_SUMMARY:
                return (
                    <FormGroup>
                        <BackupDataImportSummaryForm 
                            survey={survey} 
                            dataImportSummary={this.state.dataImportSummary}
                            selectedRecordsToImportIds={this.state.selectedRecordsToImportIds}
                            handleAllRecordsToImportSelect={this.handleAllRecordsToImportSelect}
                            handleRecordsToImportRowSelect={this.handleRecordsToImportRowSelect}
                            handleConflictingRecordsRowSelect={this.handleConflictingRecordsRowSelect}
                            selectedConflictingRecordsIds={this.state.selectedConflictingRecordsIds}
                            />
                        <Row>
                            <Col sm={{offset: 5, size: 2}} colSpan={2}>
                                <Button onClick={this.handleImportButtonClick} className="btn btn-success">{L.l('global.import.label')}</Button>
                            </Col>
                        </Row>
                    </FormGroup>
                )
            default: 
                throw new Error('Import step not supported: ' + this.state.importStep)
        }
    }
}

const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(BackupDataImportPage);
