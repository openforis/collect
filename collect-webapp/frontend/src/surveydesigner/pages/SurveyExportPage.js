import React, { Component } from 'react';
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import ServiceFactory from 'services/ServiceFactory';
import * as JobActions from 'actions/job';
import L from 'utils/Labels'

const surveyTypes = {
    temporary: 'TEMPORARY',
    published: 'PUBLISHED'
}

const outputFormats = {
    desktop: 'DESKTOP',
    earth: 'EARTH',
    mobile: 'MOBILE'
}

class SurveyExportPage extends Component {

    constructor(props) {
        super(props)


        this.state = {
            surveySummary: null,
            outputFormat: outputFormats.desktop,
            surveyType: null,
            skipValidation: true,
            outputSurveyDefaultLanguage: null,
            availableLanguages: []
        }

        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleExportModalOkButtonClick = this.handleExportModalOkButtonClick.bind(this)
    }

    componentDidMount() {
        const { surveys } = this.props

        if (surveys) {
            this.extractSurveySummaryFromPath()
        }
    }

    componentDidUpdate() {
        const { surveys } = this.props
        const { surveySummary } = this.state

        if (surveys && !surveySummary) {
            this.extractSurveySummaryFromPath()
        }
    }

    extractSurveySummaryFromPath() {
        const { surveys, location } = this.props

        const path = location.pathname
        const surveyId = parseInt(path.substring(path.lastIndexOf('/') + 1), 10)
        const surveySummary = surveys.find(s => s.id === surveyId)

        if (surveySummary) {
            this.setState({
                surveySummary,
                surveyType: surveySummary.temporary ? surveyTypes.temporary : surveyTypes.published,
                availableLanguages: surveySummary.languages && surveySummary.languages.length > 0
                    ? surveySummary.languages 
                    : [L.DEFAULT_LANG_CODE], //TODO don't default it!
                outputSurveyDefaultLanguage: surveySummary.defaultLanguage
            })
        }
    }

    handleExportButtonClick() {
        if (!this.validateForm()) {
            return
        }
        const { surveySummary, surveyType, outputFormat, outputSurveyDefaultLanguage, skipValidation } = this.state

        const languageCode = outputFormat === outputFormats.earth || outputFormat === outputFormats.mobile 
            ? outputSurveyDefaultLanguage
            : null
        
        ServiceFactory.surveyService.startExport(
            surveySummary.id,
            surveySummary.uri,
            surveyType,
            outputFormat,
            languageCode,
            skipValidation
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
        const { surveySummary, availableLanguages, surveyType, outputFormat, outputSurveyDefaultLanguage } = this.state
        if (!surveySummary) {
            return <div>Loading...</div>
        }

        const surveyTypeCheckBoxes = Object.values(surveyTypes).map(type =>
            <FormGroup>
                <Label key={type} check>
                    <Input type="radio" value={type} name="surveyType"
                        checked={surveyType === type}
                        onChange={(event) => this.setState({ surveyType: event.target.value })}
                        disabled={(type === surveyTypes.published && surveySummary.temporary && !surveySummary.publishedId)
                            || (type === surveyTypes.temporary && !surveySummary.temporary)}
                    />
                    {L.l('survey.surveyType.' + type.toLowerCase())}
                </Label>
            </FormGroup>
        )
        const outputFormatCheckboxes = Object.values(outputFormats).map(mode =>
            <FormGroup>
                <Label key={mode} check>
                    <Input type="radio" value={mode} name="outputFormat"
                        checked={outputFormat === mode}
                        onChange={(event) => this.setState({ outputFormat: event.target.value })} />
                    {L.l('survey.export.mode.' + mode.toLowerCase())}
                </Label>
            </FormGroup>
        )
        const languageOptions = availableLanguages.map(l => <option key={l} value={l}>{L.l('languages.' + l)}</option>)

        return (
            <Container>
                <FormGroup tag="fieldset" className="centered" style={{ width: '600px' }}>
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
                        {(outputFormat === outputFormats.mobile || outputFormat === outputFormats.earth) &&
                            <FormGroup row>
                                <Label for="defaultLanguage" sm={4}>{L.l(outputFormat === outputFormats.mobile ? 'survey.defaultLanguage' : 'survey.language')}:</Label>
                                <Col sm={8}>
                                    <Input type="select" name="defaultLanguage" id="defaultLanguage" value={outputSurveyDefaultLanguage}
                                        onChange={(event) => this.setState({ outputSurveyDefaultLanguage: event.target.value })}>
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
        surveys: state.surveyDesigner.surveysList.items
    }
}

export default connect(mapStateToProps)(SurveyExportPage)