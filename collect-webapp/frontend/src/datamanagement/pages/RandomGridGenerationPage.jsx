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
import Dialogs from 'common/components/Dialogs'

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
    fieldColSpan = 3,
    inputStyle = undefined,
    inputType = undefined,
    labelColSpan = 2,
    labelPrefix = '',
    label = undefined,
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
      label={label ?? labelPrefix + fieldId}
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
  const survey = SurveySelectors.useSurvey()

  const measurementAttrDef = survey?.schema?.firstRootEntityDefinition?.measurementAttributeDefinition
  const measurementAttrName = measurementAttrDef?.name

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

  const startJob = useCallback(
    (countOnly) => {
      const onRandomRecordsOnlyCountsJobCompleted = (job) => {
        setTimeout(() => {
          dispatch(JobActions.closeJobMonitor())
          if (job.completed) {
            const { recordsCount } = job.result
            if (recordsCount) {
              Dialogs.confirm(
                L.l('global.confirm'),
                L.l(`${randomGridLabelPrefix}confirmGenerateMessage`, [
                  recordsCount,
                  measurementAttrName,
                  newMeasurement,
                ]),
                startJob
              )
            } else {
              Dialogs.alert(L.l(`${randomGridLabelPrefix}noRecordsMatchingFilter`))
            }
          }
        }, 200)
      }
      ServiceFactory.recordService
        .startRandomRecordsGenerationJob({
          surveyId,
          oldMeasurement,
          newMeasurement,
          percentage,
          sourceGridSurveyFileName,
          countOnly,
        })
        .then((job) => {
          dispatch(
            JobActions.startJobMonitor({
              jobId: job.id,
              title: `${randomGridLabelPrefix}jobTitle`,
              handleJobCompleted: countOnly ? onRandomRecordsOnlyCountsJobCompleted : null,
            })
          )
        })
    },
    [surveyId, oldMeasurement, newMeasurement, percentage, sourceGridSurveyFileName]
  )

  const onGenerateClick = useCallback(() => {
    const validationResults = InputFieldValidator.validateFields({ object: state, validationsByField })
    setState((statePrev) => ({ ...statePrev, validations: validationResults }))
    if (!Objects.isEmpty(validationResults)) return

    if (state.oldMeasurement === state.newMeasurement) {
      Dialogs.alert(
        L.l('common.warning'),
        L.l(`${randomGridLabelPrefix}formValidationError.newMeasurementCannotBeEqualToOldMeasurement`, [
          measurementAttrName,
        ])
      )
    } else {
      startJob(true)
    }
  }, [measurementAttrName, startJob, state])

  return (
    <Container>
      <Form>
        <FormGroup tag="fieldset">
          <legend>{L.l('common.parameters')}</legend>
          <FormGroup row>
            <FormItemWithInput
              fieldId="oldMeasurement"
              label={L.l('dataManagement.randomGrid.oldMeasurement', [measurementAttrName])}
              state={state}
              setState={setState}
              validations={validations}
            />
            <FormItemWithInput
              fieldId="newMeasurement"
              label={L.l('dataManagement.randomGrid.newMeasurement', [measurementAttrName])}
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
