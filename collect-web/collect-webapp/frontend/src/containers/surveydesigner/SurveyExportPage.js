import React, { Component } from 'react';
import {
    Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container,
    Form, FormGroup, Label, Input, Row, Col
} from 'reactstrap';
import { connect } from 'react-redux';

import ServiceFactory from 'services/ServiceFactory';
import Workflow from 'model/Workflow';
import * as JobActions from 'actions/job';
import Objects from 'utils/Objects'
import L from 'utils/Labels'

class SurveyExportPage extends Component {

    constructor(props) {
        super(props)

        
        this.state = {
            surveySummary: null,
            outputFormat: 'DESKTOP',
            surveyType: null,
            languageCode: null,
            skipValidation: true
        }

        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleExportModalOkButtonClick = this.handleExportModalOkButtonClick.bind(this)
    }

    componentWillReceiveProps(nextProps) {
        const { surveySummaries } = nextProps
        if (surveySummaries) {
            const path = this.props.location.pathname
            const surveyId = parseInt(path.substring(path.lastIndexOf('/') + 1), 10)
            const surveySummary = surveySummaries.find(s => s.id === surveyId)
            
            this.setState({
                surveySummary: surveySummary,
                surveyType: surveySummary.temporary ? 'TEMPORARY': 'PUBLISHED'
            })
        }
    }

    handleExportButtonClick() {
        if (!this.validateForm()) {
            return
        }
        const surveySummary = this.state.surveySummary

        ServiceFactory.surveyService.startExport(
                this.state.surveySummary.id, 
                this.state.surveySummary.uri,
                this.state.surveyType, 
                this.state.outputFormat,
                this.state.languageCode,
                this.state.skipValidation
            ).then(job => {
            this.props.dispatch(JobActions.startJobMonitor({
                jobId: job.id,
                title: 'Exporting survey',
                okButtonLabel: 'Download file',
                handleOkButtonClick: this.handleExportModalOkButtonClick
            }))
        })
    }

    validateForm() {
        return true
    }

    handleExportModalOkButtonClick(job) {
        if (job.completed) {
            const surveySummary = this.state.surveySummary
            const surveyId = surveySummary.id
            ServiceFactory.surveyService.downloadExportResult(surveyId)
        }
        this.props.dispatch(JobActions.closeJobMonitor())
    }

    render() {
        const { surveySummary } = this.state
        if (! surveySummary) {
            return <div>Loading...</div>
        }

        const surveyTypes = ['TEMPORARY', 'PUBLISHED']
        const surveyTypeCheckBoxes = surveyTypes.map(type =>
            <Label key={type} check>
                <Input type="radio" value={type} name="surveyType"
                    checked={this.state.surveyType === type}
                    onChange={(event) => this.setState({ ...this.state, surveyType: event.target.value })}
                    disabled={type === 'PUBLISHED' && !surveySummary.published || type === 'TEMPORARY' && !surveySummary.temporary}
                    />
                {L.l('survey.export.surveyType.' + type.toLowerCase())}
            </Label>
        )
        const outputFormats = ['DESKTOP', 'EARTH', 'MOBILE']
        const outputFormatCheckboxes = outputFormats.map(mode =>
            <Label key={mode} check>
                <Input type="radio" value={mode} name="outputFormat"
                    checked={this.state.outputFormat === mode}
                    onChange={(event) => this.setState({ ...this.state, outputFormat: event.target.value })} />
                {L.l('survey.export.mode.' + mode.toLowerCase())}
            </Label>
        )
        return (
            <div>
                <FormGroup tag="fieldset">
                    <legend>Parameters</legend>
                    <Form>
                        <FormGroup row>
                            <Label for="surveyType" sm={1}>Survey Type:</Label>
                            <Col sm={10}>
                                <FormGroup check>
                                    {surveyTypeCheckBoxes}
                                </FormGroup>
                            </Col>
                        </FormGroup>
                        <FormGroup row>
                            <Label for="outputFormat" sm={1}>Export mode:</Label>
                            <Col sm={10}>
                                <FormGroup check>
                                    {outputFormatCheckboxes}
                                </FormGroup>
                            </Col>
                        </FormGroup>
                    </Form>
                </FormGroup>
                <Row>
                    <Col sm={{ size: 'auto', offset: 5 }}>
                        <Button onClick={this.handleExportButtonClick} className="btn btn-success">{L.l('global.export.label')}</Button>
                    </Col>
                </Row>
            </div>
        )
    }
}


const mapStateToProps = state => {
	return {
		loggedUser: state.session ? state.session.loggedUser : null,
		surveySummaries: state.surveySummaries ? state.surveySummaries.items : null
	}
}

export default connect(mapStateToProps)(SurveyExportPage)