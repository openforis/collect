import React from 'react'
import { Label } from 'reactstrap'

import InfoIcon from 'common/components/InfoIcon'

const NodeDefLabel = (props) => {
  const { nodeDefinition } = props
  const { labelOrName, description } = nodeDefinition

  return (
    <Label title={description}>
      {labelOrName}
      {description && <InfoIcon />}
    </Label>
  )
}

export default NodeDefLabel
