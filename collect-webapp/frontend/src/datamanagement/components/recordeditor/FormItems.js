import React from 'react'
import { Fade } from 'reactstrap'
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
  const visible = relevant || !hideWhenNotRelevant || parentEntity.hasSomeDescendantNotEmpty({ nodeDefinition })

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

    // include only items in record version and non-hidden calculated attributes
    const itemDefsVisible = itemDefs.filter((itemDef) => {
      const { nodeDefinition } = itemDef
      const { calculated, hidden } = nodeDefinition
      return !(calculated && hidden) && (version === null || nodeDefinition.isInVersion(version))
    })

    const firstDef = itemDefsVisible[0]

    const onlyOneMultipleEntity =
      itemDefsVisible.length === 1 &&
      (firstDef instanceof TableDefinition || firstDef instanceof MultipleFieldsetDefinition)

    if (onlyOneMultipleEntity) {
      return <FormItemsItem itemDef={firstDef} parentEntity={parentEntity} fullSize />
    }
    if (itemDefsVisible.length > 0) {
      return (
        <div className="form-items">
          {itemDefsVisible.map((itemDef) => (
            <FormItemsItem key={itemDef.id} itemDef={itemDef} parentEntity={parentEntity} />
          ))}
        </div>
      )
    }
    return null
  }
}
