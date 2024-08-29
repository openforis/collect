import { useCallback, useEffect, useMemo, useState } from 'react'
import { useDispatch } from 'react-redux'
import { Button, Col, Container, Form, FormGroup, Input, Row } from 'reactstrap'

import * as JobActions from 'actions/job'
import { SimpleFormItem } from 'common/components/Forms'
import ServiceFactory from 'services/ServiceFactory'
import { SurveySelectors } from 'store/survey'
import L from 'utils/Labels'

const randomGridLabelPrefix = 'dataManagement.randomGrid.'

const FormItemWithInput = (props) => {
  const {
    fieldId,
    fieldColSpan = 2,
    inputStyle = undefined,
    inputType = undefined,
    labelColSpan = 2,
    labelPrefix = '',
    inputOptions = [],
    validations = [],
    state,
    setState,
  } = props
  const errorFeedback = validations[fieldId]
  const invalid = !!errorFeedback
  return (
    <SimpleFormItem
      fieldId={fieldId}
      errorFeedback={errorFeedback}
      label={labelPrefix + fieldId}
      labelColSpan={labelColSpan}
      fieldColSpan={fieldColSpan}
    >
      <Input
        invalid={invalid}
        onChange={(e) => {
          setState((statePrev) => ({ ...statePrev, [fieldId]: e.target.value }))
        }}
        style={inputStyle}
        type={inputType}
        value={state[fieldId]}
      >
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
    validations: [],
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
            title: `${randomGridLabelPrefix}title`,
            handleJobCompleted: onJobComplete,
          })
        )
      })
  }, [surveyId, oldMeasurement, newMeasurement, percentage, sourceGridSurveyFileName])

  return (
    <Container>
      <Form>
        <FormGroup row>
          <FormItemWithInput
            fieldId="oldMeasurement"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
          />
          <FormItemWithInput
            fieldId="newMeasurement"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
          />
        </FormGroup>
        <FormGroup row>
          <FormItemWithInput
            fieldId="percentage"
            inputType="number"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
          />
          <FormItemWithInput
            fieldId="sourceGrid"
            inputOptions={sourceGridFilesOptions}
            inputStyle={{ width: '400px' }}
            inputType="select"
            labelPrefix={randomGridLabelPrefix}
            state={state}
            setState={setState}
          />
        </FormGroup>
        <Row>
          <Col sm={{ size: 'auto', offset: 5 }}>
            <Button onClick={startJob} className="btn btn-success">
              {L.l(`${randomGridLabelPrefix}generate`)}
            </Button>
          </Col>
        </Row>
      </Form>
    </Container>
  )
}
