import { useState } from 'react'

import * as Validations from 'model/Validations'
import { NodeCountUpdatedEvent, NodeCountValidationUpdatedEvent } from 'model/event/RecordEvent'

import { useRecordEvent } from 'common/hooks'

export const useCardinalityValidation = ({ nodeDefinition, parentEntity }) => {
  const [cardinalityValidation, setCardinalityValidation] = useState(
    Validations.getCardinalityValidation({ nodeDefinition, parentEntity })
  )

  useRecordEvent({
    parentEntity,
    onEvent: (event) => {
      if (
        (event instanceof NodeCountValidationUpdatedEvent || event instanceof NodeCountUpdatedEvent) &&
        event.isRelativeToNodes({ parentEntity, nodeDefId: nodeDefinition.id })
      ) {
        setCardinalityValidation(Validations.getCardinalityValidation({ nodeDefinition, parentEntity }))
      }
    },
  })

  return { cardinalityValidation }
}
