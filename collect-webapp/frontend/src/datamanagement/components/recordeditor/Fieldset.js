import React from 'react'

import FormItems from './FormItems'
import TabSetContent from './TabSetContent'

const Fieldset = (props) => {
  const { itemDef, parentEntity } = props

  const entityDefinition = itemDef.entityDefinition
  const entity = parentEntity ? parentEntity.getSingleChild(entityDefinition.id) : null

  return entity ? (
    <fieldset>
      <legend>{itemDef.label}</legend>
      <FormItems itemDefs={itemDef.items} parentEntity={entity} />
      <TabSetContent tabSetDef={itemDef} parentEntity={entity} />
    </fieldset>
  ) : null
}

export default Fieldset
