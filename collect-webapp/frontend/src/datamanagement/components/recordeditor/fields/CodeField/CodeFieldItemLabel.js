import React from 'react'

import Strings from 'utils/Strings'

import InfoIcon from 'common/components/InfoIcon'

export const itemLabelFunction = (attributeDefinition) => (item) => {
  const { code, label } = item
  const { showCode } = attributeDefinition

  const parts = []
  if (showCode || Strings.isBlank(label)) {
    parts.push(code)
  }
  if (Strings.isNotBlank(label)) {
    parts.push(label)
  }
  return parts.join(' - ')
}

const CodeFieldItemLabel = (props) => {
  const { item, attributeDefinition } = props

  return (
    <span className="code-field-item-label" title={item.description}>
      {itemLabelFunction(attributeDefinition)(item)}
      {item.description && <InfoIcon />}
    </span>
  )
}

export default CodeFieldItemLabel
