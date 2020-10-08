import React from 'react'

import FormItems from './FormItems'
import TabSetContent from './TabSetContent'

const Fieldset = (props) => {
  const { fieldsetDef, parentEntity } = props

  const entityDefinition = fieldsetDef.entityDefinition
  const entity = parentEntity ? parentEntity.getSingleChild(entityDefinition.id) : null

  return entity ? (
    <fieldset>
      <legend>{fieldsetDef.label}</legend>
      <FormItems itemDefs={fieldsetDef.items} parentEntity={entity} />
      <TabSetContent tabSetDef={fieldsetDef} parentEntity={entity} />
    </fieldset>
  ) : null
}

export default Fieldset
