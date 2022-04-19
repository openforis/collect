import React, { Component } from 'react';
import { Col, Container, Form, FormGroup, Label, Input, Row } from 'reactstrap';
import Button from '@mui/material/Button';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { connect } from 'react-redux';

import * as JobActions from 'actions/job';
import Dialogs from 'common/components/Dialogs'
import Dropzone from 'common/components/Dropzone';
import { SimpleFormItem } from 'common/components/Forms';
import ServiceFactory from 'services/ServiceFactory';
import L from 'utils/Labels';
import Dates from 'utils/Dates';

class RestorePage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            restoreIntoNewSurvey: false,
            restoreMode: "NEW",
            fileSelected: false,
            fileToBeImported: null,
            fileToBeImportedPreview: null,
            uploadingFile: false,
            validateRecords: true,
            deleteRecordsBeforeRestore: false
        }

        this.handleRestoreButtonClick = this.handleRestoreButtonClick.bind(this)
        this.handleFileDrop = this.handleFileDrop.bind(this)
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

    handleFileDrop(file) {
        this.setState({fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name})
    }

    handleRestoreButtonClick() {
        const { fileToBeImported, restoreMode, validateRecords, deleteAllRecordsBeforeImport } = this.state
        const { dispatch, survey } = this.props
        const surveyName = restoreMode === 'SELECTED' && survey ? survey.name : null

        this.setState({
            uploadingFile: true
        })

        ServiceFactory.backupRestoreService.startRestore(fileToBeImported, surveyName, validateRecords, deleteAllRecordsBeforeImport).then(resp => {
            this.setState({
                uploadingFile: false
            })
            if (resp.status === 'OK') {
                dispatch(JobActions.startJobMonitor({
                    jobId: resp.jobId, 
                    title: L.l('restore.restoringData'),
                    okButtonLabel: L.l('global.done')
                }))
            } else {
                Dialogs.alert(L.l('restore.errorRestoringData'), L.l(resp.errorMessage))
            }
        })
    }

    render() {
        const { survey, surveys } = this.props
        const { restoreMode, latestBackupInfo, uploadingFile, fileSelected, fileToBeImportedPreview, 
            validateRecords, deleteRecordsBeforeRestore } = this.state
        const restoreButtonEnabled = !uploadingFile && fileSelected && ((restoreMode === 'SELECTED' && survey) || restoreMode === 'NEW')
        return (
            <Container>
                <Row>
                    <Dropzone 
                        acceptedFileTypes={'.collect-backup'}
                        acceptedFileTypesDescription={L.l('restore.acceptedFileTypesDescription')}
                        handleFileDrop={this.handleFileDrop}
                        height="300px"
                        selectedFilePreview={fileToBeImportedPreview} />
                </Row>
                <Form>
                    <FormGroup tag="fieldset" style={{width: '650px'}}>
                        <legend>{L.l('restore.restoreMode')}</legend>
                        <FormGroup check>
                            <Label check>
                                <Input type="radio" name="restoreMode" value="NEW"
                                    checked={restoreMode === 'NEW'}
                                    onChange={e => this.setState({restoreMode: e.target.value})} />{' '}
                                {L.l('restore.restoreMode.newSurvey')}
                            </Label>
                        </FormGroup>
                        <FormGroup check>
                            <Label check>
                                <Input type="radio" name="restoreMode" value="SELECTED" 
                                    disabled={surveys.length === 0}
                                    checked={restoreMode === 'SELECTED'}
                                    onChange={e => this.setState({restoreMode: e.target.value})} />{' '}
                                {L.l('restore.restoreMode.selectedSurvey') + ' (' + (survey ? survey.name : L.l('survey.selectPublishedSurveyFirst')) + ')' }
                            </Label>
                        </FormGroup>

                        {restoreMode === 'SELECTED' && latestBackupInfo && 
                            <FormGroup tag="fieldset" style={{width: '600px'}}>
                                <legend>{L.l('backup.lastBackup')}</legend>
                                <SimpleFormItem label={'backup.lastBackup.date'} labelColSpan={3} fieldColSpan={5}>
                                    <Input type="text" readOnly value={Dates.formatDatetime(latestBackupInfo.date)} />
                                </SimpleFormItem>
                                <SimpleFormItem label={'backup.lastBackup.updatedRecordsSinceLastBackup'} labelColSpan={7} fieldColSpan={5}>
                                    <Input type="text" readOnly value={latestBackupInfo.updatedRecordsSinceBackup} />
                                </SimpleFormItem>
                            </FormGroup>
                        }
                    </FormGroup>
                    <FormGroup row>
                        <Accordion>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                <Typography>{L.l('general.additionalOptions')}</Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                                <div>
                                    <FormGroup row check>
                                        <Label check>
                                            <Input type="checkbox" checked={validateRecords}
                                                onChange={e => this.setState({validateRecords: e.target.checked})} /> {L.l('restore.validateRecords')}
                                        </Label>
                                    </FormGroup>
                                    <FormGroup row check>
                                        <Label check>
                                            <Input type="checkbox" checked={deleteRecordsBeforeRestore}
                                                onChange={e => this.setState({deleteRecordsBeforeRestore: e.target.checked})} /> {L.l('restore.deleteRecordsBeforeRestore')}
                                        </Label>
                                    </FormGroup>
                                </div>
                            </AccordionDetails>
                        </Accordion>
                    </FormGroup>
                    <br/>
                    <br/> 
                    <br/>
                    <Row>
                        <Col md={{offset: 5}}>
                            <Button variant="contained" color="primary" disabled={!restoreButtonEnabled} onClick={this.handleRestoreButtonClick}>{L.l('restore')}</Button>
                        </Col>
                    </Row>
                </Form>
            </Container>
        )
    }
}

const mapStateToProps = state => {
	return {
        survey: state.activeSurvey ? state.activeSurvey.survey : null,
        surveys: state.surveyDesigner.surveysList ? state.surveyDesigner.surveysList.items : null
	}
}

export default connect(mapStateToProps)(RestorePage)