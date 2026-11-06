import React, { useState } from 'react'
import classNames from 'classnames'

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
  const { item, attributeDefinition, singleLine = false } = props

  const [labelTruncated, setLabelTruncated] = useState(false)

  const label = itemLabelFunction(attributeDefinition)(item)
  const hasDescription = Boolean(item.description)
  const tooltip = `${labelTruncated ? label : ''}
    ${Strings.trimToEmpty(item.description)}`

  return (
    <span
      key={item.id}
      className={classNames('code-field-item-label', {
        'single-line': singleLine,
        'with-info-icon': hasDescription,
      })}
      title={tooltip}
    >
      {singleLine ? (
        <span
          className="text"
          onMouseEnter={(event) => setLabelTruncated(event.currentTarget.scrollWidth > event.currentTarget.clientWidth)}
        >
          {label}
        </span>
      ) : (
        label
      )}
      {hasDescription && <InfoIcon />}
    </span>
  )
}

export default CodeFieldItemLabel
