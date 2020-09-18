import React, { Component } from 'react'
import RecordEditForm from '../../datamanagement/components/RecordEditForm'
import { Record } from '../../model/Record'

import { RecordUpdater } from '../../model/RecordUpdater'
import Workflow from '../../model/Workflow'
import ServiceFactory from '../../services/ServiceFactory'

export default class SurveyDataEntryPreviewPage extends Component {
  recordUpdater = null

  constructor() {
    super()
    this.state = {
      record: null,
    }
  }

  componentDidMount() {
    const { id: idParam } = this.props.match.params
    const surveyId = Number(idParam)

    ServiceFactory.surveyService.fetchById(surveyId).then((survey) => {
      ServiceFactory.recordService
        .createRecord({ surveyId, step: Workflow.STEPS.cleansing, preview: true })
        .then((recordRes) => {
          const record = new Record(survey, recordRes)
          this.recordUpdater = new RecordUpdater(record)
          this.setState({ record })
        })
    })
  }

  componentWillUnmount() {
    if (this.recordUpdater) {
      this.recordUpdater.destroy()
    }
  }

  render() {
    const { record } = this.state

    return <RecordEditForm record={record} />
  }
}
