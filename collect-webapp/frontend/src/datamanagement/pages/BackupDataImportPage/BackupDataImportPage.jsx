import React, { Component } from 'react'
import { Button, Form, FormGroup, Row, Col } from 'reactstrap'
import { connect } from 'react-redux'

import { withNavigate } from 'common/hooks'
import Dialogs from 'common/components/Dialogs'
import Dropzone from 'common/components/Dropzone'
import UploadFileButton from 'common/components/UploadFileButton'

import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'
import L from 'utils/Labels'

import * as JobActions from 'actions/job'
import * as UserActions from 'actions/users'

import BackupDataImportSummaryForm from './BackupDataImportSummaryForm'

const importSteps = {
  selectParameters: 'selectParameters',
  showImportSummary: 'showImportSummary',
}

const defaultState = {
  importStep: importSteps.selectParameters,
  fileSelected: false,
  fileToBeImportedPreview: null,
  fileToBeImported: null,
  uploadingFile: false,
  uploadRequest: null,
  fileUploadProgressPercent: 0,
  fileUploadLoadedBytes: 0,
  fileUploadTotalBytes: 0,
  selectedRecordsToImport: [],
  selectedRecordsToImportIds: [],
  selectedConflictingRecords: [],
  selectedConflictingRecordsIds: [],
}

class BackupDataImportPage extends Component {
  constructor(props) {
    super(props)

    this.state = defaultState
    this.onFileDrop = this.onFileDrop.bind(this)
    this.handleGenerateSummaryButtonClick = this.handleGenerateSummaryButtonClick.bind(this)
    this.handleRecordSummaryGenerationComplete = this.handleRecordSummaryGenerationComplete.bind(this)
    this.handleImportButtonClick = this.handleImportButtonClick.bind(this)
    this.handleDataImportCompleteOkButtonClick = this.handleDataImportCompleteOkButtonClick.bind(this)
    this.handleRecordsToImportSelectedIdsChange = this.handleRecordsToImportSelectedIdsChange.bind(this)
    this.handleConflictingRecordsSelectedIdsChange = this.handleConflictingRecordsSelectedIdsChange.bind(this)
  }

  componentDidUpdate(prevProps) {
    const { survey } = this.props
    const { survey: prevSurvey } = prevProps
    if (survey && prevSurvey && survey.id !== prevSurvey.id) {
      this.setState(defaultState)
    }
  }

  handleFileUploadProgress(event) {
    const { total, loaded, percent } = event
    this.setState({
      fileUploadLoadedBytes: loaded,
      fileUploadTotalBytes: total,
      fileUploadProgressPercent: percent,
    })
  }

  handleGenerateSummaryButtonClick() {
    const { survey } = this.props
    const { fileToBeImported } = this.state

    const uploadRequest = ServiceFactory.recordService.generateBackupDataImportSummary(
      survey,
      survey.schema.firstRootEntityDefinition.name,
      fileToBeImported,
      () => {
        Dialogs.alert(L.l('global.error'), L.l('global.uploadingFile.error'))
        this.resetUploadingState()
      },
      (event) => this.handleFileUploadProgress(event)
    )
    uploadRequest.then((res) => {
      this.resetUploadingState()
      const job = res.body
      this.props.dispatch(
        JobActions.startJobMonitor({
          jobId: job.id,
          title: L.l('dataManagement.backupDataImport.generatingDataImportSummary'),
          okButtonLabel: L.l('global.done'),
          handleJobCompleted: this.handleRecordSummaryGenerationComplete,
        })
      )
    })
    this.setState({
      uploadingFile: true,
      uploadRequest,
    })
  }

  resetUploadingState() {
    this.setState({
      uploadingFile: false,
      uploadRequest: null,
      fileUploadProgressPercent: 0,
      fileUploadLoadedBytes: 0,
      fileUploadTotalBytes: 0,
    })
  }

  cancelImportFileUpload() {
    this.state.uploadRequest.abort()
    this.resetUploadingState()
  }

  onFileDrop(file) {
    this.setState({ fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name })
  }

  handleRecordSummaryGenerationComplete() {
    const $this = this
    ServiceFactory.recordService.loadBackupDataImportSummary(this.props.survey).then((summary) => {
      this.props.dispatch(JobActions.closeJobMonitor())
      $this.setState({
        importStep: importSteps.showImportSummary,
        dataImportSummary: summary,
        selectedRecordsToImport: summary.recordsToImport,
        selectedRecordsToImportIds: summary.recordsToImport.map((item) => item.entryId),
      })
    })
  }

  handleRecordsToImportSelectedIdsChange(selectedIds) {
    const newSelectedRecordsToImport = selectedIds.map((selectedId) =>
      this.state.dataImportSummary.recordsToImport.find((recordToImport) => recordToImport.entryId === selectedId)
    )
    this.setState({
      selectedRecordsToImport: newSelectedRecordsToImport,
      selectedRecordsToImportIds: selectedIds,
    })
  }

  handleConflictingRecordsSelectedIdsChange(selectedIds) {
    const newSelectedConflictingRecords = selectedIds.map((selectedId) =>
      this.state.dataImportSummary.conflictingRecords.find(
        (conflictingRecord) => conflictingRecord.entryId === selectedId
      )
    )
    this.setState({
      selectedConflictingRecords: newSelectedConflictingRecords,
      selectedConflictingRecordsIds: selectedIds,
    })
  }

  handleImportButtonClick() {
    ServiceFactory.recordService
      .startBackupDataImportFromSummary(
        this.props.survey,
        this.state.selectedRecordsToImportIds.concat(this.state.selectedConflictingRecordsIds),
        true
      )
      .then((job) => {
        this.props.dispatch(
          JobActions.startJobMonitor({
            jobId: job.id,
            title: L.l('dataManagement.backupDataImport.importingData'),
            okButtonLabel: L.l('global.done'),
            handleOkButtonClick: this.handleDataImportCompleteOkButtonClick,
          })
        )
      })
  }

  handleDataImportCompleteOkButtonClick() {
    this.props.dispatch(UserActions.fetchUsers())
    RouterUtils.navigateToDataManagementHomePage(this.props.navigate)
  }

  render() {
    const { survey } = this.props
    const {
      importStep,
      fileUploadProgressPercent,
      fileToBeImportedPreview,
      fileSelected,
      uploadingFile,
      fileUploadLoadedBytes,
      fileUploadTotalBytes,
      dataImportSummary,
      selectedRecordsToImportIds,
      selectedConflictingRecordsIds,
    } = this.state
    if (survey == null) {
      return <div>Select a survey first</div>
    }
    const acceptedFileTypesDescription = L.l('dataManagement.backupDataImport.acceptedFileTypesDescription')

    switch (importStep) {
      case importSteps.selectParameters:
        return (
          <Form>
            <FormGroup row>
              <Col md={12}>
                <Dropzone
                  acceptedFileTypes={'.collect-backup,.collect-data,.zip'}
                  acceptedFileTypesDescription={acceptedFileTypesDescription}
                  handleFileDrop={this.onFileDrop}
                  height="300px"
                  selectedFilePreview={fileToBeImportedPreview}
                />
              </Col>
            </FormGroup>
            {fileSelected && (
              <FormGroup row>
                <Col md={{ offset: 4, size: 4 }} colSpan={4} className="text-align-center">
                  <UploadFileButton
                    uploading={uploadingFile}
                    percent={fileUploadProgressPercent}
                    totalBytes={fileUploadTotalBytes}
                    loadedBytes={fileUploadLoadedBytes}
                    label={L.l('dataManagement.backupDataImport.generateImportSummary')}
                    onClick={this.handleGenerateSummaryButtonClick}
                    onCancel={() => this.cancelImportFileUpload()}
                  />
                </Col>
              </FormGroup>
            )}
          </Form>
        )
      case importSteps.showImportSummary:
        return (
          <FormGroup>
            <BackupDataImportSummaryForm
              survey={survey}
              dataImportSummary={dataImportSummary}
              selectedRecordsToImportIds={selectedRecordsToImportIds}
              handleRecordsToImportSelectedIdsChange={this.handleRecordsToImportSelectedIdsChange}
              selectedConflictingRecordsIds={selectedConflictingRecordsIds}
              handleConflictingRecordsSelectedIdsChange={this.handleConflictingRecordsSelectedIdsChange}
              handleAllConflictingRecordsSelect={this.handleAllConflictingRecordsSelect}
              handleConflictingRecordsRowSelect={this.handleConflictingRecordsRowSelect}
            />
            <Row>
              <Col sm={{ offset: 5, size: 2 }} colSpan={2}>
                <Button onClick={this.handleImportButtonClick} className="btn btn-success">
                  {L.l('global.import.label')}
                </Button>
              </Col>
            </Row>
          </FormGroup>
        )
      default:
        throw new Error('Import step not supported: ' + importStep)
    }
  }
}

const mapStateToProps = (state) => {
  const { survey } = state.activeSurvey

  return { survey }
}

export default connect(mapStateToProps)(withNavigate(BackupDataImportPage))
