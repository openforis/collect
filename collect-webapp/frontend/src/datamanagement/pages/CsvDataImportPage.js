import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Container, Form, FormFeedback, FormGroup, Label, Input, Col } from 'reactstrap'

import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogTitle from '@material-ui/core/DialogTitle'
import ExpansionPanel from '@material-ui/core/ExpansionPanel'
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary'
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import Typography from '@material-ui/core/Typography'

import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table'

import Dropzone from '../../common/components/Dropzone'

import ServiceFactory from 'services/ServiceFactory'
import SchemaTreeView from '../components/SchemaTreeView'
import Workflow from 'model/Workflow'
import Arrays from 'utils/Arrays'
import * as JobActions from 'actions/job'
import L from 'utils/Labels'

const importTypes = {
  newRecords: 'newRecords',
  update: 'update',
  multipleFiles: 'multipleFiles',
}

const supportedSingleFileTypes = {
  csv: {
    label: 'CSV',
    extensions: ['csv'],
  },
  excel: {
    label: 'MS Excel',
    extensions: ['xls', 'xlsx'],
  },
}

/*
const supportedMultipleFileTypes = {
    zip: {
        label: 'ZIP',
        extensions: ['zip']
    }
}
*/

const supportedSingleFilesDescription = Object.keys(supportedSingleFileTypes)
  .map((typeKey) => {
    const type = supportedSingleFileTypes[typeKey]
    return type.label + ' (' + type.extensions.map((e) => '.' + e).join(', ') + ')'
  })
  .join(', ')

const supportedSingleFileExtensionsLabel = Object.keys(supportedSingleFileTypes)
  .map((typeKey) => {
    const type = supportedSingleFileTypes[typeKey]
    return type.extensions.map((e) => '.' + e).join(',')
  })
  .join(',')

class CsvDataImportPage extends Component {
  constructor(props) {
    super(props)

    this.handleStepCheck = this.handleStepCheck.bind(this)
    this.handleEntitySelect = this.handleEntitySelect.bind(this)
    this.handleImportButtonClick = this.handleImportButtonClick.bind(this)
    this.handleFileDrop = this.handleFileDrop.bind(this)
    this.handleJobModalOkButtonClick = this.handleJobModalOkButtonClick.bind(this)
    this.handleDataImportComplete = this.handleDataImportComplete.bind(this)
    this.handleDataImoprtJobFailed = this.handleDataImoprtJobFailed.bind(this)
    this.handleErrorsModalCloseButtonClick = this.handleErrorsModalCloseButtonClick.bind(this)

    this.state = {
      importType: importTypes.update,
      selectedSteps: Workflow.STEP_CODES,
      validateRecords: true,
      deleteEntitiesBeforeImport: false,
      newRecordVersionName: null,
      fileSelected: false,
      fileToBeImportedPreview: null,
      fileToBeImported: null,
      errorModalOpen: false,
      selectedEntityDefinition: null,
    }
  }

  handleEntitySelect(event) {
    this.setState({
      ...this.state,
      selectedEntityDefinition: event.selectedNodeDefinitions.length === 1 ? event.selectedNodeDefinitions[0] : null,
    })
  }

  handleStepCheck(event) {
    const newSelectedSteps = Arrays.addOrRemoveItem(this.state.selectedSteps, event.target.value, !event.target.checked)
    this.setState({ selectedSteps: newSelectedSteps })
  }

  handleImportButtonClick() {
    if (!this.validateForm()) {
      return
    }
    const survey = this.props.survey
    const entityDef =
      this.state.importType === importTypes.newRecords
        ? survey.schema.firstRootEntityDefinition
        : this.state.selectedEntityDefinition
    const entityDefId = entityDef === null ? null : entityDef.id

    ServiceFactory.recordService
      .startCsvDataImport(
        survey,
        survey.schema.firstRootEntityDefinition.name,
        this.state.fileToBeImported,
        this.state.importType,
        this.state.selectedSteps,
        entityDefId,
        this.state.validateRecords,
        this.state.deleteEntitiesBeforeImport,
        this.state.newRecordVersionName
      )
      .then((job) => {
        this.props.dispatch(
          JobActions.startJobMonitor({
            jobId: job.id,
            title: 'Importing data',
            okButtonLabel: 'Ok',
            handleOkButtonClick: this.handleJobModalOkButtonClick,
            handleJobCompleted: this.handleDataImportComplete,
            handleJobFailed: this.handleDataImoprtJobFailed,
          })
        )
      })
  }

  validateForm() {
    if (!this.state.fileSelected) {
      alert(L.l('dataManagement.csvDataImport.validation.fileNotSelected'))
      return false
    }
    if (this.state.importType === importTypes.update && this.state.selectedEntityDefinition === null) {
      alert(L.l('dataManagement.csvDataImport.validation.entityNotSelected'))
      return false
    }
    return true
  }

  handleJobModalOkButtonClick() {}

  handleDataImportComplete(job) {}

  handleDataImoprtJobFailed(job) {
    this.loadErrorsPage(job)
  }

  handleFileDrop(file) {
    this.setState({ fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name })
  }

  loadErrorsPage(job) {
    ServiceFactory.recordService.loadCsvDataImportStatus(this.props.survey).then((job) => {
      if (job.errors.length > 0) {
        this.setState({ errorModalOpen: true, errors: job.errors })
        setTimeout(() => this.props.dispatch(JobActions.closeJobMonitor()))
      }
    })
  }

  handleErrorsModalCloseButtonClick() {
    this.setState({ errorModalOpen: false })
  }

  render() {
    const survey = this.props.survey
    if (survey === null) {
      return <div>Select a survey first</div>
    }
    const { importType } = this.state

    const importTypeOptions = Object.keys(importTypes).map((type) => (
      <option key={type} value={type}>
        {L.l('dataManagement.csvDataImport.importType.' + type)}
      </option>
    ))

    const steps = Workflow.STEPS
    const stepsChecks = Object.keys(steps).map((s) => {
      const checked = this.state.selectedSteps.indexOf(s) >= 0
      return (
        <Label check key={s}>
          <Input type="checkbox" value={s} checked={checked} onChange={this.handleStepCheck} />{' '}
          {L.l(`dataManagement.workflow.step.${s.toLocaleLowerCase()}`)}
        </Label>
      )
    })

    const entitySelectionEnabled = importType === importTypes.update
    const entityNotSelected = entitySelectionEnabled && this.state.selectedEntityDefinition === null
    const acceptedFileTypes = importType === importTypes.multipleFiles ? '.zip' : supportedSingleFileExtensionsLabel
    const acceptedFileTypesDescription =
      importType === importTypes.multipleFiles ? 'ZIP (.zip)' : supportedSingleFilesDescription

    const formatErrorMessage = function (cell, row) {
      return L.l(row.message, row.messageArgs)
    }

    const formatErrorType = function (cell, row) {
      return L.l('dataManagement.csvDataImport.error.type.' + cell)
    }

    return (
      <Container>
        <Form>
          <FormGroup tag="fieldset">
            <legend>Parameters</legend>
            <FormGroup row>
              <Label for="importType">{L.l('dataManagement.csvDataImport.importType.label')}:</Label>
              <Col sm={10}>
                <Input
                  type="select"
                  name="importType"
                  id="importType"
                  value={importType}
                  onChange={(e) => this.setState({ importType: e.target.value })}
                >
                  {importTypeOptions}
                </Input>
              </Col>
            </FormGroup>
            {importType !== importTypes.newRecords && (
              <FormGroup row>
                <Label>{L.l('dataManagement.csvDataImport.applyToSteps')}:</Label>
                <Col sm={10}>{stepsChecks}</Col>
              </FormGroup>
            )}
            {entitySelectionEnabled && (
              <FormGroup row>
                <Label className={entityNotSelected ? 'invalid' : ''}>
                  {L.l('dataManagement.csvDataImport.entity')}:
                </Label>
                <Col sm={{ size: 10 }}>
                  <SchemaTreeView survey={this.props.survey} handleNodeSelect={this.handleEntitySelect} />
                  {entityNotSelected && (
                    <FormFeedback>{L.l('dataManagement.csvDataImport.validation.entityNotSelected')}</FormFeedback>
                  )}
                </Col>
              </FormGroup>
            )}
            <FormGroup row>
              <Label for="file">File:</Label>
              <Col sm={10}>
                <Dropzone
                  acceptedFileTypes={acceptedFileTypes}
                  acceptedFileTypesDescription={acceptedFileTypesDescription}
                  handleFileDrop={(file) => this.handleFileDrop(file)}
                  height="300px"
                  selectedFilePreview={this.state.fileToBeImportedPreview}
                />
              </Col>
            </FormGroup>
            <FormGroup row>
              <ExpansionPanel>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>{L.l('general.additionalOptions')}</Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails>
                  <div>
                    <FormGroup row check>
                      <Label check>
                        <Input
                          type="checkbox"
                          checked={this.state.validateRecords}
                          onChange={(e) => this.setState({ validateRecords: e.target.checked })}
                        />{' '}
                        {L.l('dataManagement.csvDataImport.validateRecords')}
                      </Label>
                    </FormGroup>
                    <FormGroup row check>
                      <Label check>
                        <Input
                          type="checkbox"
                          checked={this.state.deleteEntitiesBeforeImport}
                          onChange={(e) => this.setState({ deleteEntitiesBeforeImport: e.target.checked })}
                        />{' '}
                        {L.l('dataManagement.csvDataImport.deleteEntities')}
                      </Label>
                    </FormGroup>
                  </div>
                </ExpansionPanelDetails>
              </ExpansionPanel>
            </FormGroup>
          </FormGroup>
          <FormGroup row>
            <Col sm={{ size: 2, offset: 5 }}>
              <Button color="primary" variant="contained" onClick={this.handleImportButtonClick}>
                {L.l('global.import')}
              </Button>
            </Col>
          </FormGroup>

          <Dialog open={this.state.errorModalOpen} maxWidth="md" fullWidth disableBackdropClick disableEscapeKeyDown>
            <DialogTitle>{L.l('dataManagement.csvDataImport.errorsInUploadedFile')}</DialogTitle>
            <DialogContent>
              <BootstrapTable
                data={this.state.errors}
                striped
                hover
                condensed
                exportCSV
                csvFileName={'ofc_csv_data_import_errors.csv'}
              >
                <TableHeaderColumn dataField="id" isKey hidden>
                  Id
                </TableHeaderColumn>
                <TableHeaderColumn dataField="fileName" width="200" hidden={importType !== importTypes.multipleFiles}>
                  {L.l('dataManagement.csvDataImport.filename')}
                </TableHeaderColumn>
                <TableHeaderColumn dataField="row" width="80">
                  {L.l('dataManagement.csvDataImport.row')}
                </TableHeaderColumn>
                <TableHeaderColumn dataField="columns" width="150">
                  {L.l('dataManagement.csvDataImport.columns')}
                </TableHeaderColumn>
                <TableHeaderColumn dataField="errorType" width="160" dataFormat={formatErrorType}>
                  {L.l('dataManagement.csvDataImport.error-type')}
                </TableHeaderColumn>
                <TableHeaderColumn
                  dataField="message"
                  width="400"
                  dataFormat={formatErrorMessage}
                  csvFormat={formatErrorMessage}
                >
                  {L.l('dataManagement.csvDataImport.error-message')}
                </TableHeaderColumn>
              </BootstrapTable>
            </DialogContent>
            <DialogActions>
              <Button color="primary" variant="contained" onClick={this.handleErrorsModalCloseButtonClick}>
                {L.l('global.close')}
              </Button>
            </DialogActions>
          </Dialog>
        </Form>
      </Container>
    )
  }
}

const mapStateToProps = (state) => {
  const { survey } = state.activeSurvey

  return { survey }
}

export default connect(mapStateToProps)(CsvDataImportPage)
