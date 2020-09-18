import React, { Component } from 'react'
import { Container, Fade } from 'reactstrap'

import FormItem from './FormItem'
import EventQueue from '../../../model/event/EventQueue'
import { NodeRelevanceUpdatedEvent } from '../../../model/event/RecordEvent'

export default class FormItems extends Component {
  constructor(props) {
    super(props)

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
  }

  componentDidMount() {
    EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)
  }

  componentWillUnmount() {
    EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
  }

  handleRecordEventReceived(event) {
    const { parentEntity } = this.props

    if (
      event instanceof NodeRelevanceUpdatedEvent &&
      event.recordId === parentEntity.record.id &&
      event.recordStep === parentEntity.record.step &&
      Number(event.nodeId) === parentEntity.id
    ) {
      this.forceUpdate()
    }
  }

  render() {
    const { itemDefs, parentEntity } = this.props

    return (
      <Container className="formItems">
        {itemDefs.map((itemDef) => {
          const nodeDefinition = itemDef.attributeDefinition || itemDef.entityDefinition
          const relevant = parentEntity.childrenRelevanceByDefinitionId[nodeDefinition.id]
          const visible = relevant || !nodeDefinition.hideWhenNotRelevant

          return (
            visible && (
              <Fade key={itemDef.id} in={visible}>
                <FormItem parentEntity={parentEntity} itemDef={itemDef} />
              </Fade>
            )
          )
        })}
      </Container>
    )
  }
}
