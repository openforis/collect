import React from 'react'
import { Container, Fade } from 'reactstrap'
import classnames from 'classnames'

import EventQueue from 'model/event/EventQueue'
import { NodeRelevanceUpdatedEvent, RecordEvent } from 'model/event/RecordEvent'
import { TableDefinition } from 'model/ui/TableDefinition'
import { MultipleFieldsetDefinition } from 'model/ui/MultipleFieldsetDefinition'

import FormItem from './FormItem'

const FormItemsItem = (props) => {
  const { itemDef, parentEntity, fullSize } = props

  const nodeDefinition = itemDef.attributeDefinition || itemDef.entityDefinition
  const relevant = parentEntity.childrenRelevanceByDefinitionId[nodeDefinition.id]
  const visible = relevant || !nodeDefinition.hideWhenNotRelevant

  return (
    visible && (
      <Fade in={visible} className={classnames({ 'full-height': fullSize })}>
        <FormItem parentEntity={parentEntity} itemDef={itemDef} fullSize={fullSize} />
      </Fade>
    )
  )
}

FormItemsItem.defaultProps = {
  fullSize: false,
}

export default class FormItems extends React.Component {
  constructor() {
    super()

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
  }

  componentDidMount() {
    EventQueue.subscribe(RecordEvent.TYPE, this.handleRecordEventReceived)
  }

  componentWillUnmount() {
    EventQueue.unsubscribe(RecordEvent.TYPE, this.handleRecordEventReceived)
  }

  handleRecordEventReceived(event) {
    const { parentEntity } = this.props

    if (event instanceof NodeRelevanceUpdatedEvent && event.isRelativeToNode(parentEntity)) {
      this.forceUpdate()
    }
  }

  render() {
    const { itemDefs, parentEntity } = this.props

    return itemDefs.length === 1 &&
      (itemDefs[0] instanceof TableDefinition || itemDefs[0] instanceof MultipleFieldsetDefinition) ? (
      <FormItemsItem itemDef={itemDefs[0]} parentEntity={parentEntity} fullSize />
    ) : itemDefs.length > 0 ? (
      <Container className="formItems">
        {itemDefs.map((itemDef) => (
          <FormItemsItem key={itemDef.id} itemDef={itemDef} parentEntity={parentEntity} />
        ))}
      </Container>
    ) : null
  }
}
