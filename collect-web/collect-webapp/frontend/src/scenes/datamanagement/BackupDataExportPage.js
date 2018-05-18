import React, { Component } from 'react'
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap'
import ExpansionPanel, { ExpansionPanelSummary, ExpansionPanelDetails } from '@material-ui/core/ExpansionPanel'
import Typography from '@material-ui/core/Typography'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
  } from '@material-ui/core/Dialog'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table'
import { connect } from 'react-redux'

import ServiceFactory from 'services/ServiceFactory'
import * as JobActions from 'actions/job'
import L from 'utils/Labels'
import Arrays from 'utils/Arrays'
import Strings from 'utils/Strings'

class BackupDataExportPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            exportOnlyOwnedRecords: false,
            includeRecordFiles: true,
            dataBackupErrorsDialogOpen: false,
            dataBackupErrors: []
        }

        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleBackupDataExportModalOkButtonClick = this.handleBackupDataExportModalOkButtonClick.bind(this)
        this.handleDataBackupErrorsDialogClose = this.handleDataBackupErrorsDialogClose.bind(this)
        this.handleDataBackupErrorsDialogConfirm = this.handleDataBackupErrorsDialogConfirm.bind(this)
        this.handleBackupDataExportJobCompleted = this.handleBackupDataExportJobCompleted.bind(this)
        this.downloadExportedFile = this.downloadExportedFile.bind(this)
    }

    handleExportButtonClick() {
        const survey = this.props.survey
        const surveyId = survey.id
        
        const backupExportParams = {
            includeRecordFiles: this.state.includeRecordFiles,
            onlyOwnedRecords: this.state.exportOnlyOwnedRecords
        }
        ServiceFactory.recordService.startBackupDataExport(surveyId, backupExportParams).then(job => {
            this.props.dispatch(JobActions.startJobMonitor({
                jobId: job.id,
                title: L.l('dataManagement.backupDataExport.exportingDataJobTitle'),
                okButtonLabel: L.l('general.download'),
                handleJobCompleted: this.handleBackupDataExportJobCompleted,
                handleOkButtonClick: this.handleBackupDataExportModalOkButtonClick
            }))
        })
    }

    handleBackupDataExportJobCompleted(job) {
        const survey = this.props.survey
        const surveyId = survey.id
        ServiceFactory.recordService.getBackupDataExportJob(surveyId).then(backupJob => {
            if (Arrays.isNotEmpty(backupJob.extras.dataBackupErrors)) {
                setTimeout(() => {
                    this.props.dispatch(JobActions.closeJobMonitor())
                    
                    this.setState({
                        dataBackupErrorsDialogOpen: true,
                        dataBackupErrors: backupJob.extras.dataBackupErrors
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
            dataBackupErrors: []
        })
    }

    handleDataBackupErrorsDialogConfirm() {
        this.downloadExportedFile()
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
                    <ExpansionPanel>
                        <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                            <Typography>{L.l('general.additionalOptions')}</Typography>
                        </ExpansionPanelSummary>
                        <ExpansionPanelDetails>
                            <div>
                                <FormGroup row>
                                    <Col sm={{size: 12}}>
                                        <Label check>
                                            <Input type="checkbox" onChange={event => this.setState({exportOnlyOwnedRecords: event.target.checked})} 
                                                checked={this.state.exportOnlyOwnedRecords} />{' '}
                                            {L.l('dataManagement.backupDataExport.exportOnlyOwnedRecords')}
                                        </Label>
                                    </Col>
                                </FormGroup>
                                <FormGroup row>
                                    <Col sm={{size: 12}}>
                                        <Label check>
                                            <Input type="checkbox" onChange={event => this.setState({includeRecordFiles: event.target.checked})} 
                                                checked={this.state.includeRecordFiles} />{' '}
                                            {L.l('dataManagement.backupDataExport.includeUploadedFiles')}
                                        </Label>
                                    </Col>
                                </FormGroup>
                            </div>
                        </ExpansionPanelDetails>
                    </ExpansionPanel>
                    <Row>
                        <Col sm={{ size: 'auto', offset: 5 }}>
                            <Button onClick={this.handleExportButtonClick} className="btn btn-success">{L.l('global.export')}</Button>
                        </Col>
                    </Row>
                </Form>

                <Dialog
                    open={dataBackupErrorsDialogOpen}
                    onClose={this.handleDataBackupErrorsDialogClose}
                    aria-labelledby="form-dialog-title">
                    <DialogTitle id="form-dialog-title">{L.l('dataManagement.backupDataExport.errorsDuringExport')}</DialogTitle>
                    <DialogContent>
                        <BootstrapTable 
                            data={errors}
                            height='300'
                            exportCSV>
                            <TableHeaderColumn dataField='recordId' width={100} isKey>ID</TableHeaderColumn>
                            <TableHeaderColumn dataField='recordKeys' width={100}>{L.l('dataManagement.backupDataExport.errors.recordKey')}</TableHeaderColumn>
                            <TableHeaderColumn dataField='recordStep' width={100}>{L.l('dataManagement.backupDataExport.errors.recordStep')}</TableHeaderColumn>
                            <TableHeaderColumn dataField='errorMessage' width={400}>{L.l('dataManagement.backupDataExport.errors.errorMessage')}</TableHeaderColumn>
                        </BootstrapTable>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.handleDataBackupErrorsDialogClose} color="secondary">{L.l('global.close')}</Button>
                        <Button onClick={this.handleDataBackupErrorsDialogConfirm} color="warning">{L.l('dataManagement.backupDataExport.downloadAnyway')}</Button>
                    </DialogActions>
                </Dialog>
            </Container>
        )
    }
}

const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(BackupDataExportPage)