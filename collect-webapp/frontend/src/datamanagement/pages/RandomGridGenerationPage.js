import { useState } from 'react'
import { useCallback } from 'react'
import { Button, Col, Container, Form, FormGroup, Input, Label, Row } from 'reactstrap'
import ServiceFactory from 'services/ServiceFactory'

import { SurveySelectors } from 'store/survey'
import L from 'utils/Labels'

export const RandomGridGenerationPage = () => {
  const surveyId = SurveySelectors.useSurveyId()

  const [state, setState] = useState({
    oldMeasurement: '',
    newMeasurement: '',
    percentage: 0,
    inputGridSurveyFileName: '',
    outputGridSurveyFileName: '',
  })

  const { oldMeasurement, newMeasurement, percentage, inputGridSurveyFileName, outputGridSurveyFileName } = state

  const onJobComplete = useCallback(() => {}, [])

  const startJob = useCallback(() => {
    ServiceFactory.recordService
      .startRandomRecordsGenerationJob({
        surveyId,
        oldMeasurement,
        newMeasurement,
        percentage,
        inputGridSurveyFileName,
        outputGridSurveyFileName,
      })
      .then((job) => {
        startJobMonitor({
          jobId: job.id,
          title: 'dataManagement.generateRandomGrid.title',
          handleJobCompleted: onJobComplete,
        })
      })
  }, [surveyId, oldMeasurement, newMeasurement, percentage, inputGridSurveyFileName, outputGridSurveyFileName])

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
