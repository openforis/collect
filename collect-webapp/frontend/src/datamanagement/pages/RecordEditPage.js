import React, { Component } from 'react'
import { connect } from 'react-redux'

import { RecordUpdater } from 'model/RecordUpdater'
import ServiceFactory from 'services/ServiceFactory'

import RecordEditForm from '../components/RecordEditForm'

class RecordEditPage extends Component {
  recordUpdater = null

  constructor() {
    super()
    this.state = {
      record: null,
    }
  }

  componentDidMount() {
    const { match, language } = this.props
    const { id: idParam } = match.params

    const recordId = Number(idParam)

    ServiceFactory.recordService.fetchSurveyId(recordId).then((res) => {
      const surveyId = Number(res)

      ServiceFactory.surveyService.fetchById(surveyId, language).then((survey) => {
        ServiceFactory.recordService.fetchById(survey, recordId).then((record) => {
          this.recordUpdater = new RecordUpdater(record)
          this.setState({ record })
        })
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
const mapStateToProps = (state) => {
  const { activeSurvey } = state
  const { survey, language } = activeSurvey

  return { survey, language }
}

export default connect(mapStateToProps)(RecordEditPage)
