import { useCallback, useEffect, useState } from 'react'
import { useDispatch } from 'react-redux'
import { Button, Col, Container, Form, FormGroup, Input, Label, Row } from 'reactstrap'

import * as JobActions from 'actions/job'
import ServiceFactory from 'services/ServiceFactory'
import { SurveySelectors } from 'store/survey'
import L from 'utils/Labels'

export const RandomGridGenerationPage = () => {
  const dispatch = useDispatch()
  const surveyId = SurveySelectors.useSurveyId()

  const [state, setState] = useState({
    oldMeasurement: '',
    newMeasurement: '',
    percentage: 0,
    sourceGridSurveyFileName: '',
    gridFiles: [],
  })

  const { oldMeasurement, newMeasurement, percentage, sourceGridSurveyFileName, gridFiles } = state

  const gridFileNames = gridFiles.map(({ fileName }) => fileName)

  const sourceGridFilesOptions = ['', ...gridFileNames].map((fileName) => (
    <option key={fileName} value={fileName}>
      {fileName}
    </option>
  ))

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
            title: 'dataManagement.randomGrid.title',
            handleJobCompleted: onJobComplete,
          })
        )
      })
  }, [surveyId, oldMeasurement, newMeasurement, percentage, sourceGridSurveyFileName])

  return (
    <Container>
      <Form>
        <FormGroup row>
          <Label md={2}>{L.l('dataManagement.randomGrid.oldMeasurement')}</Label>
          <Col md={2}>
            <Input
              value={oldMeasurement}
              onChange={(e) => {
                setState((statePrev) => ({ ...statePrev, oldMeasurement: e.target.value }))
              }}
            />
          </Col>
          <Label md={2}>{L.l('dataManagement.randomGrid.newMeasurement')}</Label>
          <Col md={2}>
            <Input
              value={newMeasurement}
              onChange={(e) => {
                setState((statePrev) => ({ ...statePrev, newMeasurement: e.target.value }))
              }}
            />
          </Col>
        </FormGroup>
        <FormGroup row>
          <Label md={2}>{L.l('dataManagement.randomGrid.percentage')}</Label>
          <Col md={2}>
            <Input
              type="number"
              value={percentage}
              onChange={(e) => {
                setState((statePrev) => ({ ...statePrev, percentage: e.target.value }))
              }}
            />
          </Col>
        </FormGroup>
        <FormGroup row>
          <Label md={2}>{L.l('dataManagement.randomGrid.sourceGrid')}</Label>
          <Col md={2}>
            <Input
              type="select"
              style={{ width: '400px' }}
              value={sourceGridSurveyFileName}
              onChange={(e) => {
                setState((statePrev) => ({ ...statePrev, sourceGridSurveyFileName: e.target.value }))
              }}
            >
              {sourceGridFilesOptions}
            </Input>
          </Col>
        </FormGroup>

        <Row>
          <Col sm={{ size: 'auto', offset: 5 }}>
            <Button onClick={startJob} className="btn btn-success">
              {L.l('dataManagement.randomGrid.generate')}
            </Button>
          </Col>
        </Row>
      </Form>
    </Container>
  )
}
