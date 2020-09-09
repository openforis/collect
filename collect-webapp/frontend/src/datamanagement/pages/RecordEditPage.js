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

  componentDidMount() {
    const { id: idParam } = this.props.match.params
    const recordId = Number(idParam)

    ServiceFactory.recordService.fetchSurveyId(recordId).then((res) => {
      const surveyId = Number(res)
      ServiceFactory.surveyService.fetchById(surveyId).then((survey) => {
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
    if (!record) {
      return <div>Loading...</div>
    }
    const { survey } = record
    const { uiConfiguration } = survey
    const tabSetDefinition = uiConfiguration.getTabSetByRootEntityDefinitionId(record.rootEntity.definition.id)

    return <TabSet tabSetDef={tabSetDefinition} record={record} />
  }
}

RecordEditPage.propTypes = {}
