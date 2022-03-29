import React, { Component } from 'react'
import { connect } from 'react-redux'

import { RecordUpdater } from 'model/RecordUpdater'
import ServiceFactory from 'services/ServiceFactory'

import RecordEditForm from '../components/RecordEditForm'
import RouterUtils from 'utils/RouterUtils'
import { withLocation, withParams } from 'common/hooks'

class RecordEditPage extends Component {
  recordUpdater = null

  constructor(props) {
    super(props)

    this.state = {
      record: null,
      inPopUp: false,
    }
  }

  async componentDidMount() {
    const { params, language: languageProps, loggedUser, location } = this.props
    const { id: idParam } = params

    const recordId = Number(idParam)

    const urlSearchParams = new URLSearchParams(location.search)
    const inPopUp = urlSearchParams.get('inPopUp') === 'true'
    const locale = urlSearchParams.get('locale')
    const localeLangCode = locale?.split('_')[0]
    const language = languageProps ? languageProps : localeLangCode

    const surveyIdRes = await ServiceFactory.recordService.fetchSurveyId(recordId)
    const surveyId = Number(surveyIdRes)

    const survey = await ServiceFactory.surveyService.fetchById(surveyId, language)
    const record = await ServiceFactory.recordService.fetchById(survey, recordId)
    const { userInGroupRole } = survey
    
    record.readOnly = !loggedUser.canEditRecord({ record, userInGroupRole })
    this.recordUpdater = new RecordUpdater(record)
    this.setState({ record, inPopUp })
  }

  componentWillUnmount() {
    this.recordUpdater?.destroy()
  }

  componentDidUpdate(propsPrev) {
    const { history, survey } = this.props
    const { survey: surveyPrev } = propsPrev

    if (survey && surveyPrev && survey.id !== surveyPrev.id) {
      RouterUtils.navigateToDataManagementHomePage(history)
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

export default connect(mapStateToProps)(withLocation(withParams(RecordEditPage)))
