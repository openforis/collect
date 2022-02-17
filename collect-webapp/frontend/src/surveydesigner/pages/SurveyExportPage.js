import React, { Component } from 'react'
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap'
import { connect } from 'react-redux'

import { withLocation } from 'common/hooks'
import ServiceFactory from 'services/ServiceFactory'
import * as JobActions from 'actions/job'
import L from 'utils/Labels'
import Languages from 'utils/Languages'

const surveyTypes = {
  temporary: 'TEMPORARY',
  published: 'PUBLISHED',
}

const outputFormats = {
  desktop: 'DESKTOP',
  earth: 'EARTH',
  mobile: 'MOBILE',
  rdb: 'RDB',
}

const rdbDialects = {
  standard: 'STANDARD',
  sqlite: 'SQLITE',
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
      availableLanguages: [],
      includeData: false,
      rdbDialect: rdbDialects.standard,
      rdbTargetSchemaName: null,
      rdbDateTimeFormat: 'yyyy-MM-dd HH:mm',
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
    const surveySummary = surveys.find((s) => s.id === surveyId)

    if (surveySummary) {
      this.setState({
        surveySummary,
        surveyType: surveySummary.temporary ? surveyTypes.temporary : surveyTypes.published,
        availableLanguages:
          surveySummary.languages && surveySummary.languages.length > 0
            ? surveySummary.languages
            : [L.DEFAULT_LANG_CODE], //TODO don't default it!
        outputSurveyDefaultLanguage: surveySummary.defaultLanguage,
        rdbTargetSchemaName: surveySummary.name,
      })
    }
  }

  handleExportButtonClick() {
    if (!this.validateForm()) {
      return
    }
    const {
      surveySummary,
      surveyType,
      outputFormat,
      outputSurveyDefaultLanguage,
      skipValidation,
      includeData,
      rdbDialect,
      rdbTargetSchemaName,
      rdbDateTimeFormat,
    } = this.state

    const languageCode =
      outputFormat === outputFormats.earth || outputFormat === outputFormats.mobile ? outputSurveyDefaultLanguage : null

    ServiceFactory.surveyService
      .startExport({
        surveyId: surveySummary.id,
        surveyUri: surveySummary.uri,
        surveyType,
        outputFormat,
        languageCode,
        skipValidation,
        includeData,
        rdbDialect,
        rdbTargetSchemaName,
        rdbDateTimeFormat,
      })
      .then((job) => {
        this.props.dispatch(
          JobActions.startJobMonitor({
            jobId: job.id,
            title: 'Exporting survey',
            okButtonLabel: 'Download file',
            handleOkButtonClick: this.handleExportModalOkButtonClick,
          })
        )
      })
  }

  validateForm() {
    return true
  }

  handleExportModalOkButtonClick(job) {
    if (job.completed) {
      const surveySummary = this.state.surveySummary
      ServiceFactory.surveyService.downloadExportResult(surveySummary.id)
    }
    this.props.dispatch(JobActions.closeJobMonitor())
  }

  render() {
    const {
      surveySummary,
      availableLanguages,
      surveyType,
      outputFormat,
      outputSurveyDefaultLanguage,
      includeData,
      rdbDialect,
      rdbTargetSchemaName,
      rdbDateTimeFormat,
    } = this.state
    if (!surveySummary) {
      return <div>Loading...</div>
    }

    const surveyTypeRadioBoxes = Object.values(surveyTypes).map((type) => (
      <FormGroup key={type}>
        <Label check>
          <Input
            type="radio"
            value={type}
            name="surveyType"
            checked={surveyType === type}
            onChange={(event) => {
              const value = event.target.value
              const newState = {
                surveyType: value,
              }
              if (value === surveyTypes.temporary) {
                // Temporary surveys cannot have data
                newState.includeData = false
              }
              this.setState(newState)
            }}
            disabled={
              (type === surveyTypes.published && surveySummary.temporary && !surveySummary.publishedId) ||
              (type === surveyTypes.temporary && !surveySummary.temporary)
            }
          />
          {L.l('survey.surveyType.' + type.toLowerCase())}
        </Label>
      </FormGroup>
    ))
    const outputFormatRadioBoxes = Object.values(outputFormats).map((mode) => (
      <FormGroup key={mode}>
        <Label check>
          <Input
            type="radio"
            value={mode}
            name="outputFormat"
            checked={outputFormat === mode}
            onChange={(event) => this.setState({ outputFormat: event.target.value })}
          />
          {L.l('survey.export.mode.' + mode.toLowerCase())}
        </Label>
      </FormGroup>
    ))
    const rdbDialectRadioBoxes = Object.values(rdbDialects).map((dialect) => (
      <FormGroup key={dialect}>
        <Label check>
          <Input
            type="radio"
            value={dialect}
            name="rdbDialect"
            checked={rdbDialect === dialect}
            onChange={(event) => this.setState({ rdbDialect: event.target.value })}
          />
          {L.l('survey.export.mode.rdb.dialect.' + dialect.toLowerCase())}
        </Label>
      </FormGroup>
    ))
    const languageOptions = availableLanguages.map((langCode) => (
      <option key={langCode} value={langCode}>
        {Languages.label(langCode)}
      </option>
    ))

    return (
      <Container>
        <FormGroup tag="fieldset" className="centered" style={{ width: '600px' }}>
          <legend>{L.l('global.parameters')}</legend>
          <Form>
            <FormGroup row>
              <Label for="surveyType" sm={3}>
                {L.l('survey.surveyType')}:
              </Label>
              <Col sm={9}>
                <FormGroup check>{surveyTypeRadioBoxes}</FormGroup>
              </Col>
            </FormGroup>
            <FormGroup row>
              <Label for="outputFormat" sm={3}>
                {L.l('survey.export.mode')}:
              </Label>
              <Col sm={9}>
                <FormGroup check>{outputFormatRadioBoxes}</FormGroup>
              </Col>
            </FormGroup>
            {(outputFormat === outputFormats.mobile || outputFormat === outputFormats.earth) && (
              <FormGroup row>
                <Label for="defaultLanguage" sm={4}>
                  {L.l(outputFormat === outputFormats.mobile ? 'survey.defaultLanguage' : 'survey.language')}:
                </Label>
                <Col sm={8}>
                  <Input
                    type="select"
                    name="defaultLanguage"
                    id="defaultLanguage"
                    value={outputSurveyDefaultLanguage}
                    onChange={(event) => this.setState({ outputSurveyDefaultLanguage: event.target.value })}
                  >
                    {languageOptions}
                  </Input>
                </Col>
              </FormGroup>
            )}
            {outputFormat === outputFormats.rdb && (
              <>
                <FormGroup row>
                  <Label for="rdbDialect" sm={3}>
                    {L.l('survey.export.mode.rdb.dialect')}:
                  </Label>
                  <Col sm={9}>
                    <FormGroup check>{rdbDialectRadioBoxes}</FormGroup>
                  </Col>
                </FormGroup>
                <FormGroup row>
                  <Label for="rdbTargetSchemaName" sm={4}>
                    {L.l('survey.export.mode.rdb.targetSchemaName')}:
                  </Label>
                  <Col sm={8}>
                    <Input
                      name="rdbTargetSchemaName"
                      id="rdbTargetSchemaName"
                      value={rdbTargetSchemaName}
                      onChange={(event) => this.setState({ rdbTargetSchemaName: event.target.value })}
                      disabled={rdbDialect === rdbDialects.sqlite}
                    />
                  </Col>
                </FormGroup>
                <FormGroup row>
                  <Label for="includeData" sm={4}>
                    {L.l('survey.export.mode.rdb.includeData')}:
                  </Label>
                  <Col sm={8}>
                    <Input
                      type="checkbox"
                      name="includeData"
                      id="includeData"
                      checked={includeData}
                      onChange={(event) => this.setState({ includeData: event.target.value })}
                      disabled={surveyType === surveyTypes.temporary}
                    />
                  </Col>
                </FormGroup>
                {includeData && (
                  <FormGroup row>
                    <Label for="rdbDateTimeFormat" sm={4}>
                      {L.l('survey.export.mode.rdb.dateTimeFormat')}:
                    </Label>
                    <Col sm={8}>
                      <Input
                        name="rdbDateTimeFormat"
                        id="rdbDateTimeFormat"
                        value={rdbDateTimeFormat}
                        onChange={(event) => this.setState({ rdbDateTimeFormat: event.target.value })}
                        disabled={rdbDialect === rdbDialects.sqlite}
                      />
                    </Col>
                  </FormGroup>
                )}
              </>
            )}
          </Form>
        </FormGroup>
        <Row>
          <Col sm={{ size: 'auto', offset: 5 }}>
            <Button onClick={this.handleExportButtonClick} className="btn btn-success">
              {L.l('global.export.label')}
            </Button>
          </Col>
        </Row>
      </Container>
    )
  }
}

const mapStateToProps = (state) => {
  return {
    loggedUser: state.session ? state.session.loggedUser : null,
    surveys: state.surveyDesigner.surveysList.items,
  }
}

export default connect(mapStateToProps)(withLocation(SurveyExportPage))
