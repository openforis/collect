import React, { Component } from 'react'

import { RecordUpdater } from '../../model/RecordUpdater'

import ServiceFactory from 'services/ServiceFactory'

import RecordEditForm from '../components/RecordEditForm'

export default class RecordEditPage extends Component {
  recordUpdater = null

  constructor() {
    super()
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

    return <RecordEditForm record={record} />
  }
}

RecordEditPage.propTypes = {}
