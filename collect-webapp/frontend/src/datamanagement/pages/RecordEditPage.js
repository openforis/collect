import React, { Component } from 'react'
import PropTypes from 'prop-types'

import TabSet from 'datamanagement/components/recordeditor/TabSet'
import { RecordUpdater } from '../../model/RecordUpdater'

import ServiceFactory from 'services/ServiceFactory'

export default class RecordEditPage extends Component {
  recordUpdater = null

  constructor(props) {
    super(props)

    this.state = {
      record: null,
    }
  }

  static propTypes = {
    survey: PropTypes.object,
  }

  componentDidMount() {
    let idParam = this.props.match.params.id
    let recordId = parseInt(idParam)

    ServiceFactory.recordService.fetchSurveyId(recordId).then((res) => {
      let surveyId = parseInt(res)
      ServiceFactory.surveyService.fetchById(surveyId).then((survey) => {
        ServiceFactory.recordService.fetchById(survey, recordId).then((record) => {
          this.recordUpdater = new RecordUpdater(record)
          this.setState({ ...this.state, record: record })
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
    let record = this.state.record
    if (!record) {
      return <div>Loading...</div>
    }
    let survey = record.survey
    let uiConf = survey.uiConfiguration
    let tabSetDefinition = uiConf.getTabSetByRootEntityDefinitionId(record.rootEntity.definition.id)

    return <TabSet tabSetDef={tabSetDefinition} record={record} />
  }
}
