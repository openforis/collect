import React, { Component } from 'react';
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import ExpansionPanel, { ExpansionPanelSummary, ExpansionPanelDetails } from 'material-ui/ExpansionPanel';
import Typography from 'material-ui/Typography';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import { connect } from 'react-redux';

import ServiceFactory from 'services/ServiceFactory'
import * as JobActions from 'actions/job';
import L from 'utils/Labels';
import Arrays from 'utils/Arrays';

class BackupDataExportPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            exportOnlyOwnedRecords: false,
            includeRecordFiles: true
        }

        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleBackupDataExportModalOkButtonClick = this.handleBackupDataExportModalOkButtonClick.bind(this)
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
                okButtonLabel: L.l('global.done'),
                handleOkButtonClick: this.handleBackupDataExportModalOkButtonClick
            }))
        })
    }

    handleBackupDataExportModalOkButtonClick(job) {
        if (job.completed) {
            const survey = this.props.survey
            const surveyId = survey.id
            ServiceFactory.recordService.downloadBackupDataExportResult(surveyId)
        }
        this.props.dispatch(JobActions.closeJobMonitor())
    }

    render() {
        if (!this.props.survey) {
            return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
        }
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
            </Container>
        )
    }
}



const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(BackupDataExportPage);