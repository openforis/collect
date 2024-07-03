import React, { Component } from 'react'
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap'
import { connect } from 'react-redux'
import Accordion from '@mui/material/Accordion'
import AccordionSummary from '@mui/material/AccordionSummary'
import AccordionDetails from '@mui/material/AccordionDetails'
import Typography from '@mui/material/Typography'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

import ServiceFactory from 'services/ServiceFactory'
import SchemaTreeView from '../components/SchemaTreeView'
import SurveyLanguagesSelect from '../../common/components/SurveyLanguagesSelect'
import Workflow from 'model/Workflow'
import * as JobActions from 'actions/job'
import Objects from 'utils/Objects'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'

const outputFormats = {
  CSV: 'CSV',
  XLSX: 'XLSX',
}

const additionalOptions = [
  'includeKMLColumnForCoordinates',
  'includeAllAncestorAttributes',
  'includeCompositeAttributeMergedColumn',
  'includeEnumeratedEntities',
  'codeAttributeExpanded',
  'includeCodeItemLabelColumn',
  'includeCreatedByUserColumn',
  'includeGroupingLabels',
  'includeImages',
  'alwaysEvaluateCalculatedAttributes',
]

const onlyExcelAdditionalOptions = ['includeImages']

const exportModes = {
  allEntities: 'ALL_ENTITIES',
  selectedEntity: 'SELECTED_ENTITY',
}

const headingSources = {
  attributeName: 'ATTRIBUTE_NAME',
  instanceLabel: 'INSTANCE_LABEL',
  reportingLabel: 'REPORTING_LABEL',
}

const defaultState = {
  outputFormat: 'CSV',
  stepGreaterOrEqual: 'ENTRY',
  modifiedSince: '',
  modifiedUntil: '',
  exportMode: exportModes.allEntities,
  selectedEntityDefinition: null,
  entityId: null,
  exportOnlyOwnedRecords: false,
  includeRecordFiles: true,
  headingSource: headingSources.attributeName,
  languageCode: '',
  includeGroupingLabels: true,
  includeImages: false,
  alwaysEvaluateCalculatedAttributes: false,
  filterExpression: '',
}

class CsvDataExportPage extends Component {
  constructor(props) {
    super(props)

    this.state = defaultState

    this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
    this.handleCsvDataExportModalOkButtonClick = this.handleCsvDataExportModalOkButtonClick.bind(this)
    this.handleEntitySelect = this.handleEntitySelect.bind(this)
    this.handleOutputFormatChange = this.handleOutputFormatChange.bind(this)
  }

  static getDerivedStateFromProps(prevProps, prevState) {
    const { survey } = prevProps
    return prevState.languageCode
      ? {}
      : {
          languageCode: survey ? survey.defaultLanguage : '',
        }
  }

  handleExportButtonClick() {
    if (!this.validateForm()) {
      return
    }
    const { survey, rootEntityDef, keyAttributes, summaryAttributes } = this.props

    const {
      exportMode,
      outputFormat,
      stepGreaterOrEqual,
      modifiedSince,
      modifiedUntil,
      selectedEntityDefinition,
      exportOnlyOwnedRecords,
      headingSource,
      languageCode,
      filterExpression,
    } = this.state

    const surveyId = survey.id

    const keyAttributeValues = keyAttributes.map((a, idx) => this.state['key' + idx], this)
    const summaryAttributeValues = summaryAttributes.map((a, idx) => this.state['summary' + idx], this)

    const selectedEntityId =
      exportMode === exportModes.allEntities ? null : selectedEntityDefinition ? selectedEntityDefinition.id : null

    const parameters = {
      surveyId: survey.id,
      rootEntityId: rootEntityDef.id,
      outputFormat,
      stepGreaterOrEqual,
      modifiedSince,
      modifiedUntil,
      entityId: selectedEntityId,
      exportOnlyOwnedRecords,
      headingSource,
      languageCode,
      alwaysGenerateZipFile: true,
      keyAttributeValues,
      summaryAttributeValues,
      filterExpression,
    }

    additionalOptions.forEach((o) => {
      const val = this.state[o]
      parameters[o] = Objects.isNullOrUndefined(val) ? null : val
    })
    ServiceFactory.recordService.startCSVDataExport(surveyId, parameters).then((job) => {
      this.props.dispatch(
        JobActions.startJobMonitor({
          jobId: job.id,
          title: L.l('dataManagement.export.exportDialog.title'),
          okButtonLabel: L.l('dataManagement.export.exportDialog.downloadExportedFile'),
          handleOkButtonClick: this.handleCsvDataExportModalOkButtonClick,
        })
      )
    })
  }

  validateForm() {
    if (this.state.exportMode === exportModes.selectedEntity && !this.state.selectedEntityDefinition) {
      alert('Please select an entity to export')
      return false
    }
    return true
  }

  handleCsvDataExportModalOkButtonClick(job) {
    if (job.completed) {
      const survey = this.props.survey
      const surveyId = survey.id
      ServiceFactory.recordService.downloadCSVDataExportResult(surveyId)
    }
    this.props.dispatch(JobActions.closeJobMonitor())
  }

  handleEntitySelect(event) {
    const selectedEntityDefinition = Arrays.singleItemOrNull(event.selectedNodeDefinitions)
    this.setState({ ...this.state, selectedEntityDefinition: selectedEntityDefinition })
  }

  handleHeadingSourceChange(headingSource) {
    this.setState({
      headingSource,
      languageCode: headingSource === headingSources.attributeName ? '' : this.state.languageCode,
    })
  }

  handleOutputFormatChange(event) {
    this.setState((statePrev) => {
      const newOutputFormat = event.target.value
      const newState = { ...statePrev, outputFormat: newOutputFormat }
      if (newOutputFormat === outputFormats.CSV) {
        Object.values(onlyExcelAdditionalOptions).forEach((onlyExcelOption) => {
          newState[onlyExcelOption] = false
        })
      }
      return newState
    })
  }

  render() {
    const { survey, keyAttributes, summaryAttributes, loggedUser, roleInSurvey } = this.props

    if (!survey) {
      return <div>Select survey first</div>
    }
    const {
      outputFormat,
      stepGreaterOrEqual,
      exportMode,
      exportOnlyOwnedRecords,
      modifiedSince,
      modifiedUntil,
      headingSource,
      languageCode,
      filterExpression,
    } = this.state

    const additionalOptionsFormGroups = additionalOptions
      .filter((option) => !onlyExcelAdditionalOptions.includes(option) || outputFormat === outputFormats.XLSX)
      .map((option) => (
        <FormGroup check key={option}>
          <Label check>
            <Input
              type="checkbox"
              onChange={(event) => {
                this.setState({
                  [option]: event.target.checked,
                })
              }}
              checked={this.state[option]}
            />{' '}
            {L.l('dataManagement.export.additionalOptions.' + option)}
          </Label>
        </FormGroup>
      ))

    const stepsOptions = Workflow.STEP_CODES.map((stepCode) => (
      <option key={stepCode} value={stepCode}>
        {L.l(`dataManagement.workflow.step.${stepCode.toLocaleLowerCase()}`)}
      </option>
    ))

    const createAttributeFormGroup = function (context, attr, prefix, index) {
      const name = prefix + index
      const value = context.state[name]
      return (
        <FormGroup row key={name}>
          <Label md={4}>{attr.labelOrName}</Label>
          <Col md={8}>
            <Input
              name={name}
              value={value}
              onChange={(e) => {
                context.setState({ name: e.target.value })
              }}
            />
          </Col>
        </FormGroup>
      )
    }

    const keyAttributeFormGroups = keyAttributes.map((attr, i) => createAttributeFormGroup(this, attr, 'key', i))

    const filteredSummaryAttributes = summaryAttributes.filter((a) =>
      loggedUser.canFilterRecordsBySummaryAttribute(a, roleInSurvey)
    )

    const summaryFormGroups = filteredSummaryAttributes.map((attr, i) =>
      createAttributeFormGroup(this, attr, 'summary', i)
    )

    return (
      <Container>
        <Form>
          <FormGroup tag="fieldset">
            <legend>{L.l('common.parameters')}</legend>
            <FormGroup row>
              <Label md={2} for="outputFormat">
                {L.l('dataManagement.export.outputFormat')}:
              </Label>
              <Col md={10}>
                <FormGroup check>
                  {Object.values(outputFormats).map((of) => (
                    <>
                      <Label key={of} check>
                        <Input
                          type="radio"
                          value={of}
                          name="outputFormat"
                          checked={outputFormat === of}
                          onChange={this.handleOutputFormatChange}
                        />
                        {L.l(`dataManagement.export.outputFormat.${of.toLocaleLowerCase()}`)}
                      </Label>
                      <span style={{ display: 'inline-block', width: '40px' }}></span>
                    </>
                  ))}
                </FormGroup>
              </Col>
            </FormGroup>
            <FormGroup row>
              <Label md={2} for="stepSelect">
                {L.l('dataManagement.export.step')}:
              </Label>
              <Col md={10}>
                <Input
                  type="select"
                  name="step"
                  id="stepSelect"
                  style={{ maxWidth: '200px' }}
                  value={stepGreaterOrEqual}
                  onChange={(e) => this.setState({ stepGreaterOrEqual: e.target.value })}
                >
                  {stepsOptions}
                </Input>
              </Col>
            </FormGroup>
            <FormGroup row>
              <Label md={2} for="exportMode">
                {L.l('dataManagement.export.mode')}:
              </Label>
              <Col md={10}>
                <FormGroup check>
                  {Object.values(exportModes).map((mode) => (
                    <>
                      <Label key={mode} check>
                        <Input
                          type="radio"
                          value={mode}
                          name="exportMode"
                          checked={exportMode === mode}
                          onChange={(e) => this.setState({ ...this.state, exportMode: e.target.value })}
                        />
                        {L.l(`dataManagement.export.mode.${mode}`)}
                      </Label>
                      <span style={{ display: 'inline-block', width: '40px' }}></span>
                    </>
                  ))}
                </FormGroup>
              </Col>
            </FormGroup>
            {exportMode === exportModes.selectedEntity && (
              <FormGroup row>
                <Label sm={1}>{L.l('dataManagement.export.selectEntities')}:</Label>
                <Col sm={{ size: 10 }}>
                  <SchemaTreeView survey={survey} handleNodeSelect={this.handleEntitySelect} />
                </Col>
              </FormGroup>
            )}
            <FormGroup row>
              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>{L.l('dataManagement.export.filter')}</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <div>
                    <FormGroup check row>
                      <Label check>
                        <Input
                          type="checkbox"
                          onChange={(e) => this.setState({ exportOnlyOwnedRecords: e.target.checked })}
                          checked={exportOnlyOwnedRecords}
                        />
                        {L.l('dataManagement.export.onlyOwnedRecords')}
                      </Label>
                    </FormGroup>
                    <FormGroup row>
                      <Label md={3} for="modifiedSince">
                        {L.l('dataManagement.export.modifiedSince')}:
                      </Label>
                      <Col md={4}>
                        <Input
                          type="date"
                          name="modifiedSince"
                          id="modifiedSince"
                          value={modifiedSince}
                          onChange={(e) => this.setState({ modifiedSince: e.target.value })}
                        />
                      </Col>
                      <Label md={1} for="modifiedUntil">
                        {L.l('dataManagement.export.modifiedUntil')}:
                      </Label>
                      <Col md={4}>
                        <Input
                          type="date"
                          name="modifiedUntil"
                          id="modifiedUntil"
                          value={modifiedUntil}
                          onChange={(e) => this.setState({ modifiedUntil: e.target.value })}
                        />
                      </Col>
                    </FormGroup>
                    {keyAttributeFormGroups}
                    {summaryFormGroups}
                    <FormGroup row>
                      <Label md={4}>{L.l('dataManagement.export.filterExpression')}</Label>
                      <Col md={8}>
                        <Input
                          onChange={(e) => this.setState({ filterExpression: e.target.value })}
                          value={filterExpression}
                        />
                      </Col>
                    </FormGroup>
                  </div>
                </AccordionDetails>
              </Accordion>
            </FormGroup>
            <FormGroup row>
              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>{L.l('general.additionalOptions')}</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <div>
                    <FormGroup row>
                      <Col md={2}>
                        <Label for="headingsSourceSelect">{L.l('dataManagement.export.sourceForFileHeadings')}:</Label>
                      </Col>
                      <Col>
                        <Input
                          type="select"
                          name="headingsSource"
                          id="headingsSourceSelect"
                          style={{ maxWidth: '200px' }}
                          onChange={(e) => this.handleHeadingSourceChange(e.target.value)}
                        >
                          {Object.keys(headingSources).map((headingSource) => (
                            <option key={headingSource} value={headingSources[headingSource]}>
                              {L.l(`dataManagement.export.sourceForFileHeadings.${headingSource}`)}
                            </option>
                          ))}
                        </Input>
                      </Col>
                    </FormGroup>
                    {headingSource !== headingSources.attributeName && (
                      <FormGroup row>
                        <Col md={6}>
                          <Label>{L.l('dataManagement.export.fileHeadingsLanguage')}</Label>
                        </Col>
                        <Col md={6}>
                          <SurveyLanguagesSelect
                            survey={survey}
                            value={languageCode}
                            onChange={(languageCode) => this.setState({ languageCode })}
                          />
                        </Col>
                      </FormGroup>
                    )}
                    {additionalOptionsFormGroups}
                    <FormGroup row>
                      <Col md={12}>
                        <Label>* = {L.l('dataManagement.export.notCompatibleWithCSVDataImportNote')}</Label>
                      </Col>
                    </FormGroup>
                  </div>
                </AccordionDetails>
              </Accordion>
            </FormGroup>
          </FormGroup>
          <Row>
            <Col sm={{ size: 'auto', offset: 5 }}>
              <Button onClick={this.handleExportButtonClick} className="btn btn-success">
                {L.l('dataManagement.export')}
              </Button>
            </Col>
          </Row>
        </Form>
      </Container>
    )
  }
}

const mapStateToProps = (state) => {
  const survey = state.activeSurvey ? state.activeSurvey.survey : null
  const loggedUser = state.session ? state.session.loggedUser : null

  let rootEntityDef = null,
    keyAttributes = null,
    summaryAttributes = null,
    roleInSurvey = null

  if (survey) {
    rootEntityDef = survey.schema.firstRootEntityDefinition
    keyAttributes = rootEntityDef.keyAttributeDefinitions
    summaryAttributes = rootEntityDef.attributeDefinitionsShownInRecordSummaryList
    roleInSurvey = survey.userInGroupRole
  }

  return {
    survey,
    rootEntityDef,
    keyAttributes,
    summaryAttributes,
    roleInSurvey,
    loggedUser,
  }
}

export default connect(mapStateToProps)(CsvDataExportPage)
