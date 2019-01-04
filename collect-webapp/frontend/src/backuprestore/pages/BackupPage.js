import React, { Component } from 'react';
import { Col, Container, Form, FormGroup, Input, Row } from 'reactstrap';
import Button from '@material-ui/core/Button';
import { connect } from 'react-redux';

import * as JobActions from 'actions/job'
import { SimpleFormItem } from 'common/components/Forms'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Dates from 'utils/Dates'

class BackupPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            latestBackupInfo: null
        }

        this.handleBackupButtonClick = this.handleBackupButtonClick.bind(this)
        this.handleBackupCompleteOkButtonClick = this.handleBackupCompleteOkButtonClick.bind(this)
        this.handleDownloadLastBackupButtonClick = this.handleDownloadLastBackupButtonClick.bind(this)

        if (this.props.survey) {
            this.updateLatestBackupInfo(props.survey)
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.survey) {
            this.updateLatestBackupInfo(nextProps.survey)
        }
    }

    updateLatestBackupInfo(survey) {
        ServiceFactory.backupRestoreService.getLatestBackupInfo(survey.id).then(info => {
            this.setState({
                latestBackupInfo: info
            })
        })
    }

    handleBackupButtonClick() {
        ServiceFactory.backupRestoreService.startFullBackup(this.props.survey.id).then(job => {
            this.props.dispatch(JobActions.startJobMonitor({
                jobId: job.id, 
                title: L.l('backup.backingUpData'),
                okButtonLabel: L.l('global.done'),
                handleOkButtonClick: this.handleBackupCompleteOkButtonClick
            }))
        })
    }

    handleBackupCompleteOkButtonClick(job) {
        this.updateLatestBackupInfo(this.props.survey)
        this.props.dispatch(JobActions.closeJobMonitor())
        this.handleDownloadLastBackupButtonClick()
    }

    handleDownloadLastBackupButtonClick() {
        const survey = this.props.survey
        ServiceFactory.backupRestoreService.downloadLastBackup(survey.id)
    }

    render() {
        const { survey } = this.props
        if (!survey) {
            return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
        }
        const latestBackupInfo = this.state.latestBackupInfo
        if (latestBackupInfo === null) {
            return <div>{L.l('global.loading')}</div>
        }

        return (
            <Container>
                <Form style={{width: '600px'}}>
                    <FormGroup tag="fieldset">
                        <legend>{L.l('backup.lastBackup')}</legend>
                        <SimpleFormItem label={'backup.lastBackup.date'} labelColSpan={3} fieldColSpan={5}>
                            <Input type="text" readOnly value={Dates.formatDatetime(latestBackupInfo.date)} />
                        </SimpleFormItem>
                        <SimpleFormItem label={'backup.lastBackup.updatedRecordsSinceLastBackup'} labelColSpan={7} fieldColSpan={5}>
                            <Input type="text" readOnly value={latestBackupInfo.updatedRecordsSinceBackup} />
                        </SimpleFormItem>
                        <Col sm={{offset: 4}}>
                            <Button disabled={latestBackupInfo.date === null} variant="raised" onClick={this.handleDownloadLastBackupButtonClick}>{L.l('backup.lastBackup.download')}</Button>
                        </Col>
                    </FormGroup>    
                </Form>
                <br/> <br/> <br/>
                <Row>
                    <Col sm={{offset: 2}}>
                        <Button variant="raised" color="primary" onClick={this.handleBackupButtonClick}>{L.l('backup.generateNewBackup')}</Button>
                    </Col>
                </Row>
            </Container>
        )
    }
}

const mapStateToProps = state => {
	return {
		survey: state.activeSurvey ? state.activeSurvey.survey : null
	}
}

export default connect(mapStateToProps)(BackupPage)