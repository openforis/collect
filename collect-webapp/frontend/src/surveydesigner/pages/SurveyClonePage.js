import React, { Component } from 'react'
import { Alert, Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap'
import { connect } from 'react-redux'

import * as JobActions from 'actions/job'
import { withLocation, withNavigate } from 'common/hooks'
import { SimpleFormItem, normalizeInternalName, getValidState } from 'common/components/Forms'
import Dialogs from 'common/components/Dialogs'
import ServiceFactory from 'services/ServiceFactory'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

const NAME_SUFFIX = '_copy'

class SurveyClonePage extends Component {
  constructor(props) {
    super(props)

    this.state = {
      originalSurveySummary: null,
      originalSurveyType: null,
      newSurveyName: null,
      validating: false,
      validationErrors: null,
    }

    this.findUnusedSurveyName = this.findUnusedSurveyName.bind(this)
    this.handleNameChange = this.handleNameChange.bind(this)
    this.validateFormAsync = this.validateFormAsync.bind(this)
    this.handleCloneButtonClick = this.handleCloneButtonClick.bind(this)
    this.handleCloneModalOkButtonClick = this.handleCloneModalOkButtonClick.bind(this)
  }

  componentDidMount() {
    const { surveys } = this.props
    if (surveys && surveys.length > 0 && !this.state.originalSurveySummary) {
      const path = this.props.location.pathname
      const surveyName = path.substring(path.lastIndexOf('/') + 1)
      const surveySummary = surveys.find((s) => s.name === surveyName)
      this.setState({
        originalSurveySummary: surveySummary,
        originalSurveyType: surveySummary.temporary ? 'TEMPORARY' : 'PUBLISHED',
        newSurveyName: this.findUnusedSurveyName(surveys, surveySummary),
      })
    }
  }

  findUnusedSurveyName(surveys, surveySummary) {
    const isNameDuplicate = (name) => Arrays.contains(surveys, (s) => s.name === name)

    const newNameBase = surveySummary.name + (surveySummary.name.endsWith(NAME_SUFFIX) ? '' : NAME_SUFFIX)

    let newName = newNameBase

    if (isNameDuplicate(newName)) {
      // add count suffix to name if duplicate
      let count = 1
      while (isNameDuplicate(newName)) {
        newName = newNameBase + '_' + count++
      }
    }
    return newName
  }

  handleCloneButtonClick() {
    const { originalSurveySummary, originalSurveyType, newSurveyName } = this.state
    ServiceFactory.surveyService
      .startClone(originalSurveySummary.name, originalSurveyType, newSurveyName)
      .then((job) => {
        this.props.dispatch(
          JobActions.startJobMonitor({
            jobId: job.id,
            title: L.l('survey.clone.cloningSurvey'),
            handleOkButtonClick: this.handleCloneModalOkButtonClick,
          })
        )
      })
  }

  validateFormAsync() {
    this.setState({ validating: true })
    const { originalSurveySummary, originalSurveyType, newSurveyName } = this.state
    const originalSurveyName = originalSurveySummary.name

    ServiceFactory.surveyService.validateClone(originalSurveyName, originalSurveyType, newSurveyName).then((r) => {
      const validationErrors = r.status === 'OK' ? null : r.objects.errors
      this.setState({
        validating: false,
        validationErrors: validationErrors,
      })
    })
  }

  handleNameChange(e) {
    this.setState({ newSurveyName: normalizeInternalName(e.target.value) })
  }

  handleCloneModalOkButtonClick(job) {
    if (job.completed) {
      ServiceFactory.surveyService.getClonedSurveyId().then((res) => {
        if (res.statusOk) {
          const surveyId = res.object
          RouterUtils.navigateToSurveyEditPage(this.props.navigate, surveyId)
        } else {
          Dialogs.alert(L.l('global.error'))
        }
      })
    }
    this.props.dispatch(JobActions.closeJobMonitor())
  }

  render() {
    const { error, originalSurveySummary, originalSurveyType, newSurveyName, validating, validationErrors } = this.state
    if (!originalSurveySummary) {
      return <div>Loading...</div>
    }
    const surveyTypes = ['TEMPORARY', 'PUBLISHED']
    const surveyTypeRadioBoxes = surveyTypes.map((type) => (
      <Label key={type} check>
        <Input
          type="radio"
          value={type}
          name="originalSurveyType"
          checked={originalSurveyType === type}
          onChange={(e) => this.setState({ originalSurveyType: e.target.value })}
          disabled={
            (type === 'PUBLISHED' && !originalSurveySummary.published) ||
            (type === 'TEMPORARY' && !originalSurveySummary.temporary)
          }
        />
        {L.l('survey.surveyType.' + type.toLowerCase())}
      </Label>
    ))

    return (
      <Container>
        <FormGroup tag="fieldset">
          <legend>{L.l('survey.clone.title')}</legend>
          <Form>
            <SimpleFormItem
              fieldId="originalSurveyName"
              label={L.l('survey.clone.originalSurvey.name')}
              labelColSpan={3}
              fieldColSpan={9}
              name="originalSurveyName"
              type="text"
            >
              <Input type="text" readOnly value={originalSurveySummary.name} />
            </SimpleFormItem>
            <FormGroup row>
              <Label sm={3}>{L.l('survey.clone.originalSurvey.type')}:</Label>
              <Col sm={9}>
                <FormGroup check>{surveyTypeRadioBoxes}</FormGroup>
              </Col>
            </FormGroup>
            <SimpleFormItem
              fieldId="newSurveyName"
              label={L.l('survey.clone.newSurveyName')}
              labelColSpan={3}
              fieldColSpan={9}
              name="newSurveyName"
              validationErrors={validationErrors}
              type="text"
            >
              <Input
                type="text"
                value={newSurveyName}
                onChange={this.handleNameChange}
                onBlur={this.validateFormAsync}
                valid={getValidState('newSurveyName', validationErrors)}
              />
            </SimpleFormItem>
            {error && <Alert color="danger">{error}</Alert>}
            <Row>
              <Col xs={{ offset: 5 }}>
                <Button color="primary" disabled={validationErrors || validating} onClick={this.handleCloneButtonClick}>
                  {L.l('survey.clone')}
                </Button>
              </Col>
            </Row>
          </Form>
        </FormGroup>
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

export default connect(mapStateToProps)(withNavigate(withLocation(SurveyClonePage)))
