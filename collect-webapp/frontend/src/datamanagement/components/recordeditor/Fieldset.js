import React from 'react'

import FormItems from './FormItems'
import TabSetContent from './TabSetContent'
import NodeDefLabel from './NodeDefLabel'

const Fieldset = (props) => {
  const { itemDef, parentEntity } = props
  const { entityDefinition } = itemDef

  if (!parentEntity) return null

  const entity = parentEntity.getSingleChild(entityDefinition.id)

  return (
    <fieldset className="form-item-fieldset">
      <legend>
        <NodeDefLabel nodeDefinition={entityDefinition} limitWidth={false} />
      </legend>
      <FormItems parentItemDef={itemDef} parentEntity={entity} />
      <TabSetContent tabSetDef={itemDef} parentEntity={entity} />
    </fieldset>
  )
}

export default Fieldset
