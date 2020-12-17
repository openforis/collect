import React, { Component } from 'react'
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap'
import { connect } from 'react-redux'
import ExpansionPanel from '@material-ui/core/ExpansionPanel'
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary'
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails'
import Typography from '@material-ui/core/Typography'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'

import ServiceFactory from 'services/ServiceFactory'
import SchemaTreeView from '../components/SchemaTreeView'
import SurveyLanguagesSelect from '../../common/components/SurveyLanguagesSelect'
import Workflow from 'model/Workflow'
import * as JobActions from 'actions/job'
import Objects from 'utils/Objects'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'

const csvExportAdditionalOptions = [
  'includeKMLColumnForCoordinates',
  'includeAllAncestorAttributes',
  'includeCompositeAttributeMergedColumn',
  'includeEnumeratedEntities',
  'codeAttributeExpanded',
  'includeCodeItemLabelColumn',
  'includeCreatedByUserColumn',
  'includeGroupingLabels',
]

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
}

class CsvDataExportPage extends Component {
  constructor(props) {
    super(props)

    this.state = defaultState

    this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
    this.handleCsvDataExportModalOkButtonClick = this.handleCsvDataExportModalOkButtonClick.bind(this)
    this.handleEntitySelect = this.handleEntitySelect.bind(this)
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
    }

    csvExportAdditionalOptions.forEach((o) => {
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
    } = this.state

    const additionalOptionsFormGroups = csvExportAdditionalOptions.map((o) => (
      <FormGroup check key={o}>
        <Label check>
          <Input
            type="checkbox"
            onChange={(event) => {
              this.setState({
                [o]: event.target.checked,
              })
            }}
            checked={this.state[o]}
          />{' '}
          {L.l('dataManagement.export.additionalOptions.' + o)}
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
          <Label md={4}>{attr.label}</Label>
          <Col md={8}>
            <Input
              name={name}
              value={value}
              onChange={(e) => {
                const newState = {}
                newState[name] = e.target.value
                context.setState(newState)
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
                  <Label check>
                    <Input
                      type="radio"
                      value="CSV"
                      name="outputFormat"
                      checked={outputFormat === 'CSV'}
                      onChange={(event) => this.setState({ ...this.state, outputFormat: event.target.value })}
                    />
                    {L.l('dataManagement.export.outputFormat.csv')}
                  </Label>
                  <span style={{ display: 'inline-block', width: '40px' }}></span>
                  <Label check>
                    <Input
                      type="radio"
                      value="XLSX"
                      name="outputFormat"
                      checked={outputFormat === 'XLSX'}
                      onChange={(event) => this.setState({ ...this.state, outputFormat: event.target.value })}
                    />
                    {L.l('dataManagement.export.outputFormat.xlsx')}
                  </Label>
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
                  <Label check>
                    <Input
                      type="radio"
                      value={exportModes.allEntities}
                      name="exportMode"
                      checked={exportMode === exportModes.allEntities}
                      onChange={(event) => this.setState({ ...this.state, exportMode: event.target.value })}
                    />
                    {L.l('dataManagement.export.mode.allEntities')}
                  </Label>
                  <span style={{ display: 'inline-block', width: '40px' }}></span>
                  <Label check>
                    <Input
                      type="radio"
                      value={exportModes.selectedEntity}
                      name="exportMode"
                      checked={exportMode === exportModes.selectedEntity}
                      onChange={(event) => this.setState({ ...this.state, exportMode: event.target.value })}
                    />
                    {L.l('dataManagement.export.mode.onlySelectedEntities')}
                  </Label>
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
              <ExpansionPanel>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>{L.l('dataManagement.export.filter')}</Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails>
                  <div>
                    <FormGroup check row>
                      <Label check>
                        <Input
                          type="checkbox"
                          onChange={(event) => this.setState({ exportOnlyOwnedRecords: event.target.checked })}
                          checked={exportOnlyOwnedRecords}
                        />{' '}
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
                  </div>
                </ExpansionPanelDetails>
              </ExpansionPanel>
            </FormGroup>
            <FormGroup row>
              <ExpansionPanel>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>{L.l('general.additionalOptions')}</Typography>
                </ExpansionPanelSummary>
                <ExpansionPanelDetails>
                  <div>
                    <FormGroup row>
                      <Col md={6}>
                        <Label for="headingsSourceSelect">{L.l('dataManagement.export.sourceForFileHeadings')}:</Label>
                      </Col>
                      <Col md={6}>
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
                </ExpansionPanelDetails>
              </ExpansionPanel>
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
