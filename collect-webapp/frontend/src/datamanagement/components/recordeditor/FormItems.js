import React from 'react'
import { Container, Fade } from 'reactstrap'
import classnames from 'classnames'

import { NodeRelevanceUpdatedEvent } from 'model/event/RecordEvent'
import { TableDefinition } from 'model/ui/TableDefinition'
import { MultipleFieldsetDefinition } from 'model/ui/MultipleFieldsetDefinition'

import FormItem from './FormItem'
import AbstractFormComponent from './AbstractFormComponent'

const FormItemsItem = (props) => {
  const { itemDef, parentEntity, fullSize } = props
  const { nodeDefinition } = itemDef
  const { id: nodeDefId, hideWhenNotRelevant } = nodeDefinition
  const relevant = parentEntity.childrenRelevanceByDefinitionId[nodeDefId]
  const visible = relevant || !hideWhenNotRelevant

  const className = classnames('form-item-external-wrapper', { 'full-height': fullSize, 'not-relevant': !relevant })

  const formItem = <FormItem parentEntity={parentEntity} itemDef={itemDef} fullSize={fullSize} />

  return hideWhenNotRelevant ? (
    visible && (
      <Fade in={visible} className={className}>
        {formItem}
      </Fade>
    )
  ) : (
    <div className={className}>{formItem}</div>
  )
}

FormItemsItem.defaultProps = {
  fullSize: false,
}

export default class FormItems extends AbstractFormComponent {
  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { parentEntity } = this.props

    if (event instanceof NodeRelevanceUpdatedEvent && event.isRelativeToNode(parentEntity)) {
      this.forceUpdate()
    }
  }

  render() {
    const { itemDefs, parentEntity } = this.props
    const { record } = parentEntity
    const { version } = record
    const itemDefsInVersion =
      version === null ? itemDefs : itemDefs.filter((itemDef) => itemDef.nodeDefinition.isInVersion(version))

    const firstDef = itemDefsInVersion[0]

    const onlyOneMultipleEntity =
      itemDefsInVersion.length === 1 &&
      (firstDef instanceof TableDefinition || firstDef instanceof MultipleFieldsetDefinition)

    return onlyOneMultipleEntity ? (
      <FormItemsItem itemDef={firstDef} parentEntity={parentEntity} fullSize />
    ) : itemDefsInVersion.length > 0 ? (
      <div className="form-items">
        {itemDefsInVersion.map((itemDef) => (
          <FormItemsItem key={itemDef.id} itemDef={itemDef} parentEntity={parentEntity} />
        ))}
      </div>
    ) : null
  }
}
