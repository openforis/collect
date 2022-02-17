import { withLocation, withParams } from 'common/hooks'
import React, { Component } from 'react'
import RecordEditForm from '../../datamanagement/components/RecordEditForm'
import { Record } from '../../model/Record'

import { RecordUpdater } from '../../model/RecordUpdater'
import Workflow from '../../model/Workflow'
import ServiceFactory from '../../services/ServiceFactory'

class SurveyDataEntryPreviewPage extends Component {
  recordUpdater = null

  constructor() {
    super()
    this.state = {
      record: null,
    }
  }

  componentDidMount() {
    const { params, location } = this.props
    const { id: surveyIdParam } = params

    const urlSearchParams = new URLSearchParams(location.search)
    const versionId = urlSearchParams.get('versionId')
    const langCode = urlSearchParams.get('locale')

    const surveyId = Number(surveyIdParam)

    ServiceFactory.surveyService.fetchById(surveyId, langCode).then((survey) => {
      ServiceFactory.recordService
        .createRecord({ surveyId, step: Workflow.STEPS.cleansing, versionId, preview: true })
        .then((recordRes) => {
          const record = new Record(survey, recordRes)
          this.recordUpdater = new RecordUpdater(record)
          this.setState({ record })
        })
    })
  }

  componentWillUnmount() {
    this.recordUpdater?.destroy()
  }

  render() {
    const { record } = this.state

    return <RecordEditForm record={record} />
  }
}

export default withLocation(withParams(SurveyDataEntryPreviewPage))
