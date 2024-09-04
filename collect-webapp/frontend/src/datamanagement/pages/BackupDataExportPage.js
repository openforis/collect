import './BackupDataExportPage.scss'

import React, { Component } from 'react'
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap'
import Accordion from '@mui/material/Accordion'
import AccordionSummary from '@mui/material/AccordionSummary'
import AccordionDetails from '@mui/material/AccordionDetails'
import Typography from '@mui/material/Typography'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import { connect } from 'react-redux'

import ServiceFactory from 'services/ServiceFactory'
import * as JobActions from 'actions/job'
import L from 'utils/Labels'
import Arrays from 'utils/Arrays'
import { DataGrid } from 'common/components'
import { DataExportFilterAccordion } from 'datamanagement/components/DataExportFilterAccordion'

class BackupDataExportPage extends Component {
  constructor(props) {
    super(props)

    this.state = {
      exportOnlyOwnedRecords: false,
      includeRecordFiles: true,
      dataBackupErrorsDialogOpen: false,
      dataBackupErrors: [],
    }

    this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
    this.handleBackupDataExportModalOkButtonClick = this.handleBackupDataExportModalOkButtonClick.bind(this)
    this.handleDataBackupErrorsDialogClose = this.handleDataBackupErrorsDialogClose.bind(this)
    this.handleDataBackupErrorsDialogConfirm = this.handleDataBackupErrorsDialogConfirm.bind(this)
    this.handleBackupDataExportJobCompleted = this.handleBackupDataExportJobCompleted.bind(this)
    this.downloadExportedFile = this.downloadExportedFile.bind(this)
    this.onFilterPropChange = this.onFilterPropChange.bind(this)
  }

  handleExportButtonClick() {
    this.startExportJob(true)
  }

  startExportJob(countOnly = false) {
    const { survey } = this.props

    const { id: surveyId } = survey

    const rootEntityDef = survey.schema.firstRootEntityDefinition

    const keyAttributes = rootEntityDef.keyAttributeDefinitions
    const keyAttributeValues = keyAttributes.map((_a, idx) => this.state['key' + idx], this)

    const summaryAttributes = rootEntityDef.attributeDefinitionsShownInRecordSummaryList
    const summaryAttributeValues = summaryAttributes.map((_a, idx) => this.state['summary' + idx], this)

    const backupExportParams = {
      ...this.state,
      countOnly,
      onlyOwnedRecords: this.state.exportOnlyOwnedRecords,
      keyAttributeValues,
      summaryAttributeValues,
    }

    ServiceFactory.recordService.startBackupDataExport(surveyId, backupExportParams).then((job) => {
      this.props.dispatch(
        JobActions.startJobMonitor({
          jobId: job.id,
          title: L.l('dataManagement.backupDataExport.exportingDataJobTitle'),
          okButtonLabel: L.l('common.download'),
          handleJobCompleted: countOnly
            ? this.handleBackupDataExportDryRunJobCompleted
            : this.handleBackupDataExportJobCompleted,
          handleOkButtonClick: this.handleBackupDataExportModalOkButtonClick,
        })
      )
    })
  }

  handleBackupDataExportDryRunJobCompleted(job) {
    this.props.dispatch(JobActions.closeJobMonitor())
  }

  handleBackupDataExportJobCompleted(job) {
    const survey = this.props.survey
    const surveyId = survey.id
    ServiceFactory.recordService.getBackupDataExportJob(surveyId).then((backupJob) => {
      if (Arrays.isNotEmpty(backupJob.extras.dataBackupErrors)) {
        setTimeout(() => {
          this.props.dispatch(JobActions.closeJobMonitor())

          this.setState({
            dataBackupErrorsDialogOpen: true,
            dataBackupErrors: backupJob.extras.dataBackupErrors,
          })
        }, 500) //avoids that a reducer dispatches an action
      }
    })
  }

  handleBackupDataExportModalOkButtonClick(job) {
    if (job.completed) {
      this.downloadExportedFile()
    }
    this.props.dispatch(JobActions.closeJobMonitor())
  }

  downloadExportedFile() {
    const survey = this.props.survey
    ServiceFactory.recordService.downloadBackupDataExportResult(survey.id)
  }

  handleDataBackupErrorsDialogClose() {
    this.setState({
      dataBackupErrorsDialogOpen: false,
      dataBackupErrors: [],
    })
  }

  handleDataBackupErrorsDialogConfirm() {
    this.downloadExportedFile()
  }

  onFilterPropChange({ prop, value }) {
    this.setState({ [prop]: value })
  }

  render() {
    if (!this.props.survey) {
      return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
    }
    const { dataBackupErrorsDialogOpen, dataBackupErrors } = this.state
    const errors = dataBackupErrorsDialogOpen && dataBackupErrors ? dataBackupErrors : []
    return (
      <Container>
        <Form>
          <DataExportFilterAccordion filterObject={this.state} onPropChange={this.onFilterPropChange} />
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>{L.l('general.additionalOptions')}</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <div>
                <FormGroup row>
                  <Col sm={{ size: 12 }}>
                    <Label check>
                      <Input
                        type="checkbox"
                        onChange={(event) => this.setState({ includeRecordFiles: event.target.checked })}
                        checked={this.state.includeRecordFiles}
                      />{' '}
                      {L.l('dataManagement.backupDataExport.includeUploadedFiles')}
                    </Label>
                  </Col>
                </FormGroup>
              </div>
            </AccordionDetails>
          </Accordion>
          <Row>
            <Col sm={{ size: 'auto', offset: 5 }}>
              <Button onClick={this.handleExportButtonClick} className="btn btn-success">
                {L.l('global.export')}
              </Button>
            </Col>
          </Row>
        </Form>

        <Dialog
          open={dataBackupErrorsDialogOpen}
          onClose={this.handleDataBackupErrorsDialogClose}
          aria-labelledby="form-dialog-title"
          maxWidth="lg"
        >
          <DialogTitle id="form-dialog-title">{L.l('dataManagement.backupDataExport.errorsDuringExport')}</DialogTitle>
          <DialogContent>
            <DataGrid
              className="data-export-errors-data-grid"
              columns={[
                { field: 'recordId', width: 100, headerName: 'Record ID' },
                { field: 'recordKeys', flex: 0.3, headerName: 'dataManagement.backupDataExport.errors.recordKey' },
                { field: 'recordStep', flex: 0.3, headerName: 'dataManagement.backupDataExport.errors.recordStep' },
                {
                  field: 'errorMessage',
                  flex: 1,
                  headerName: 'dataManagement.backupDataExport.errors.errorMessage',
                },
              ]}
              getRowId={(row) => row.recordId}
              rows={errors}
              showToolbar
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleDataBackupErrorsDialogClose} color="secondary">
              {L.l('global.close')}
            </Button>
            <Button onClick={this.handleDataBackupErrorsDialogConfirm} color="warning">
              {L.l('dataManagement.backupDataExport.downloadAnyway')}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    )
  }
}

const mapStateToProps = (state) => {
  const { survey } = state.activeSurvey

  return { survey }
}

export default connect(mapStateToProps)(BackupDataExportPage)
