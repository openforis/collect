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
    const { match, language: languageProps, loggedUser, location } = this.props
    const { id: idParam } = match.params

    const recordId = Number(idParam)

    const urlSearchParams = new URLSearchParams(location.search)
    const inPopUp = urlSearchParams.get('inPopUp') === 'true'
    const locale = urlSearchParams.get('locale')
    const localeLangCode = locale?.split('_')[0]
    const language = languageProps ? languageProps : localeLangCode

    ServiceFactory.recordService.fetchSurveyId(recordId).then((res) => {
      const surveyId = Number(res)

      ServiceFactory.surveyService.fetchById(surveyId, language).then((survey) => {
        ServiceFactory.recordService.fetchById(survey, recordId).then((record) => {
          record.readOnly = !loggedUser.canEditRecord(record)
          this.recordUpdater = new RecordUpdater(record)
          this.setState({ record, inPopUp })
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
    const { record, inPopUp } = this.state

    return <RecordEditForm record={record} inPopUp={inPopUp} />
  }
}
const mapStateToProps = (state) => {
  const { activeSurvey, session } = state
  const { survey, language } = activeSurvey
  const { loggedUser } = session

  return { survey, loggedUser, language }
}

export default connect(mapStateToProps)(RecordEditPage)
