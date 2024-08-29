import { useCallback, useEffect, useMemo, useState } from 'react'
import { useDispatch } from 'react-redux'
import { Button, Col, Container, Form, FormGroup, Input, Row } from 'reactstrap'

import * as JobActions from 'actions/job'
import { SimpleFormItem } from 'common/components/Forms'
import ServiceFactory from 'services/ServiceFactory'
import { SurveySelectors } from 'store/survey'
import L from 'utils/Labels'
import Objects from 'utils/Objects'
import { InputFieldValidator } from 'validation/InputFieldValidator'

const randomGridLabelPrefix = 'dataManagement.randomGrid.'

const validationsByField = {
  oldMeasurement: [InputFieldValidator.validateRequired],
  newMeasurement: [InputFieldValidator.validateRequired],
  percentage: [InputFieldValidator.validateGreaterThan(0), InputFieldValidator.validateLessThan(100)],
  sourceGridSurveyFileName: [InputFieldValidator.validateRequired],
}

const FormItemWithInput = (props) => {
  const {
    fieldId,
    fieldColSpan = 2,
    inputStyle = undefined,
    inputType = undefined,
    labelColSpan = 2,
    labelPrefix = '',
    inputOptions = [],
    validations = {},
    state,
    setState,
  } = props
  const errorFeedback = validations[fieldId]
  const invalid = !!errorFeedback
  const onChange = useCallback(
    (e) => {
      const newValue = e.target.value
      setState((statePrev) => {
        const validationsPrev = statePrev.validations
        const stateNext = { ...statePrev, [fieldId]: newValue }
        const newValidations = {
          ...validationsPrev,
          [fieldId]: InputFieldValidator.validateField({ object: stateNext, fieldKey: fieldId, validationsByField }),
        }
        stateNext.validations = newValidations
        return stateNext
      })
    },
    [setState]
  )

  return (
    <SimpleFormItem
      fieldId={fieldId}
      errorFeedback={errorFeedback}
      label={labelPrefix + fieldId}
      labelColSpan={labelColSpan}
      fieldColSpan={fieldColSpan}
    >
      <Input invalid={invalid} onChange={onChange} style={inputStyle} type={inputType} value={state[fieldId]}>
        {inputType === 'select' ? inputOptions : undefined}
      </Input>
    </SimpleFormItem>
  )
}

export const RandomGridGenerationPage = () => {
  const dispatch = useDispatch()
  const surveyId = SurveySelectors.useSurveyId()

  const [state, setState] = useState({
    oldMeasurement: '',
    newMeasurement: '',
    percentage: 0,
    sourceGridSurveyFileName: '',
    gridFiles: [],
    validations: {},
  })

  const { oldMeasurement, newMeasurement, percentage, sourceGridSurveyFileName, gridFiles, validations } = state

  const gridFileNames = gridFiles.map(({ fileName }) => fileName)

  const sourceGridFilesOptions = useMemo(
    () =>
      ['', ...gridFileNames].map((fileName) => (
        <option key={fileName} value={fileName}>
          {fileName}
        </option>
      )),
    [gridFileNames]
  )

  useEffect(() => {
    if (surveyId) {
      ServiceFactory.surveyService.fetchSurveyFilesSummaries(surveyId).then((fileSummaries) => {
        const gridFiles = fileSummaries.filter((fileSummary) => fileSummary.type === 'COLLECT_EARTH_GRID')
        setState((statePrev) => ({ ...statePrev, gridFiles }))
      })
    }
  }, [surveyId])

  const onJobComplete = useCallback(() => {}, [])

  const startJob = useCallback(() => {
    ServiceFactory.recordService
      .startRandomRecordsGenerationJob({
        surveyId,
        oldMeasurement,
        newMeasurement,
        percentage,
        sourceGridSurveyFileName,
      })
      .then((job) => {
        dispatch(
          JobActions.startJobMonitor({
            jobId: job.id,
            title: `${randomGridLabelPrefix}jobTitle`,
            handleJobCompleted: onJobComplete,
          })
        )
      })
  }, [surveyId, oldMeasurement, newMeasurement, percentage, sourceGridSurveyFileName])

  const onGenerateClick = useCallback(() => {
    const validationResults = InputFieldValidator.validateFields({ object: state, validationsByField })
    setState((statePrev) => ({ ...statePrev, validations: validationResults }))
    if (Objects.isEmpty(validationResults)) {
      startJob()
    }
  }, [startJob, state])

  return (
    <Container>
      <Form>
        <FormGroup row>
          <FormItemWithInput
            fieldId="oldMeasurement"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
            validations={validations}
          />
          <FormItemWithInput
            fieldId="newMeasurement"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
            validations={validations}
          />
        </FormGroup>
        <FormGroup row>
          <FormItemWithInput
            fieldId="percentage"
            inputType="number"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
            validations={validations}
          />
          <FormItemWithInput
            fieldId="sourceGridSurveyFileName"
            inputOptions={sourceGridFilesOptions}
            inputStyle={{ width: '400px' }}
            inputType="select"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
            validations={validations}
          />
        </FormGroup>
        <Row>
          <Col sm={{ size: 'auto', offset: 5 }}>
            <Button onClick={onGenerateClick} className="btn btn-success">
              {L.l(`${randomGridLabelPrefix}generate`)}
            </Button>
          </Col>
        </Row>
      </Form>
    </Container>
  )
}
