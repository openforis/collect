import React from 'react'
import { Fade } from 'reactstrap'
import classnames from 'classnames'

import { NodeRelevanceUpdatedEvent } from 'model/event/RecordEvent'
import { TableDefinition } from 'model/ui/TableDefinition'
import { MultipleFieldsetDefinition } from 'model/ui/MultipleFieldsetDefinition'

import FormItem from './FormItem'
import AbstractFormComponent from './AbstractFormComponent'

const FormItemsItem = (props) => {
  const { itemDef, parentEntity, fullSize, style } = props
  const { nodeDefinition } = itemDef
  const { hideWhenNotRelevant } = nodeDefinition
  const relevant = parentEntity.isChildRelevant(nodeDefinition)
  const visible = relevant || !hideWhenNotRelevant || parentEntity.hasSomeDescendantNotEmpty({ nodeDefinition })

  const className = classnames('form-item-external-wrapper', { 'full-height': fullSize, 'not-relevant': !relevant })

  const formItem = <FormItem parentEntity={parentEntity} itemDef={itemDef} fullSize={fullSize} />

  return hideWhenNotRelevant ? (
    visible && (
      <Fade in={visible} className={className} style={style}>
        {formItem}
      </Fade>
    )
  ) : (
    <div className={className} style={style}>
      {formItem}
    </div>
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
    const { parentItemDef, parentEntity } = this.props
    const { items: itemDefs, totalColumns } = parentItemDef
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
      const gridTemplateColumns = `repeat(${totalColumns}, auto)`
      return (
        <div className="form-items" style={{ gridTemplateColumns }}>
          {itemDefsVisible.map((itemDef) => {
            const { column, columnSpan, row } = itemDef
            const itemStyle = {
              gridRowStart: row,
              gridRowEnd: row + 1,
              gridColumnStart: column,
              gridColumnEnd: column + columnSpan,
            }
            return <FormItemsItem key={itemDef.id} itemDef={itemDef} parentEntity={parentEntity} style={itemStyle} />
          })}
        </div>
      )
    }
    return null
  }
}
