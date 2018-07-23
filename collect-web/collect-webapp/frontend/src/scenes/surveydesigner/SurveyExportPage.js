import React, { Component } from 'react';
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import ServiceFactory from 'services/ServiceFactory';
import * as JobActions from 'actions/job';
import L from 'utils/Labels'

class SurveyExportPage extends Component {

    constructor(props) {
        super(props)

        
        this.state = {
            surveySummary: null,
            outputFormat: 'DESKTOP',
            surveyType: null,
            skipValidation: true,
            outputSurveyDefaultLanguage: null,
            availableLanguages: []
        }

        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleExportModalOkButtonClick = this.handleExportModalOkButtonClick.bind(this)
    }

    componentWillReceiveProps(nextProps) {
        const { surveySummaries } = nextProps
        if (surveySummaries && !this.state.surveySummary) {
            const path = this.props.location.pathname
            const surveyId = parseInt(path.substring(path.lastIndexOf('/') + 1), 10)
            const surveySummary = surveySummaries.find(s => s.id === surveyId)
            
            this.setState({
                surveySummary: surveySummary,
                surveyType: surveySummary.temporary ? 'TEMPORARY': 'PUBLISHED',
                availableLanguages: surveySummary.languages,
                outputSurveyDefaultLanguage: surveySummary.defaultLanguage
            })
        }
    }

    handleExportButtonClick() {
        if (!this.validateForm()) {
            return
        }
        ServiceFactory.surveyService.startExport(
                this.state.surveySummary.id, 
                this.state.surveySummary.uri,
                this.state.surveyType, 
                this.state.outputFormat,
                this.state.outputSurveyDefaultLanguage,
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
        const { surveySummary, availableLanguages, outputSurveyDefaultLanguage } = this.state
        if (! surveySummary) {
            return <div>Loading...</div>
        }
        
        const surveyTypes = ['TEMPORARY', 'PUBLISHED']
        const surveyTypeCheckBoxes = surveyTypes.map(type => 
            <FormGroup>
                <Label key={type} check>
                    <Input type="radio" value={type} name="surveyType"
                        checked={this.state.surveyType === type}
                        onChange={(event) => this.setState({ ...this.state, surveyType: event.target.value })}
                        disabled={(type === 'PUBLISHED' && surveySummary.temporary && !surveySummary.publishedId)
                            || (type === 'TEMPORARY' && !surveySummary.temporary)}
                        />
                    {L.l('survey.surveyType.' + type.toLowerCase())}
                </Label>
            </FormGroup>
        )
        const outputFormats = ['DESKTOP', 'EARTH', 'MOBILE']
        const outputFormatCheckboxes = outputFormats.map(mode =>
            <FormGroup>
                <Label key={mode} check>
                    <Input type="radio" value={mode} name="outputFormat"
                        checked={this.state.outputFormat === mode}
                        onChange={(event) => this.setState({ ...this.state, outputFormat: event.target.value })} />
                    {L.l('survey.export.mode.' + mode.toLowerCase())}
                </Label>
            </FormGroup>
        )
        const languageOptions = availableLanguages.map(l => <option key={l} value={l}>{L.l('languages.' + l)}</option>)

        return (
            <Container>
                <FormGroup tag="fieldset" className="centered" style={{width: '600px'}}>
                    <legend>{L.l('global.parameters')}</legend>
                    <Form>
                        <FormGroup row>
                            <Label for="surveyType" sm={3}>{L.l('survey.surveyType')}:</Label>
                            <Col sm={9}>
                                <FormGroup check>
                                    {surveyTypeCheckBoxes}
                                </FormGroup>
                            </Col>
                        </FormGroup>
                        <FormGroup row>
                            <Label for="outputFormat" sm={3}>{L.l('survey.export.mode')}:</Label>
                            <Col sm={9}>
                                <FormGroup check>
                                    {outputFormatCheckboxes}
                                </FormGroup>
                            </Col>
                        </FormGroup>
                        {(this.state.outputFormat === 'MOBILE' || this.state.outputFormat === 'EARTH') &&
                            <FormGroup row>
                                <Label for="defaultLanguage" sm={4}>{L.l(this.state.outputFormat === 'MOBILE' ? 'survey.defaultLanguage': 'survey.language')}:</Label>
                                <Col sm={8}>
                                    <Input type="select" name="defaultLanguage" id="defaultLanguage" value={outputSurveyDefaultLanguage}
                                        onChange={(event) => this.setState({...this.state, outputSurveyDefaultLanguage: event.target.value})}>
                                        {languageOptions}
                                    </Input>
                                </Col>
                            </FormGroup>
                        }
                    </Form>
                </FormGroup>
                <Row>
                    <Col sm={{ size: 'auto', offset: 5 }}>
                        <Button onClick={this.handleExportButtonClick} className="btn btn-success">{L.l('global.export.label')}</Button>
                    </Col>
                </Row>
            </Container>
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