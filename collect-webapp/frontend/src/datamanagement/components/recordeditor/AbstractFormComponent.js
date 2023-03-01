import React from 'react'

import { RecordEvent } from 'model/event/RecordEvent'
import EventQueue from 'model/event/EventQueue'

export default class AbstractFormComponent extends React.Component {
  constructor(props) {
    super(props)
    this._onRecordEvent = this._onRecordEvent.bind(this)
  }

  componentDidMount() {
    EventQueue.subscribe(RecordEvent.TYPE, this._onRecordEvent)
  }

  componentWillUnmount() {
    EventQueue.unsubscribe(RecordEvent.TYPE, this._onRecordEvent)
  }

  updateState() {
    this.setState(this.determineNewState())
  }

  determineNewState() {
    const newState = {}
    return newState
  }

  _onRecordEvent(event) {
    const { parentEntity } = this.props
    if (parentEntity) {
      this.onRecordEvent(event)
    }
  }

  onRecordEvent(event) {}
}
