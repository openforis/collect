import React, { useState } from 'react'
import classNames from 'classnames'
import Truncate from 'react-truncate'

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
  const { item, attributeDefinition, singleLine } = props

  const [labelTruncated, setLabelTruncated] = useState(false)

  const label = itemLabelFunction(attributeDefinition)(item)
  const hasDescription = Boolean(item.description)
  const tooltip = `${labelTruncated ? label : ''}
    ${Strings.trimToEmpty(item.description)}`

  return (
    <span
      className={classNames('code-field-item-label', {
        'single-line': singleLine,
        'with-info-icon': hasDescription,
      })}
      title={tooltip}
    >
      {singleLine ? (
        <span className="text">
          <Truncate lines={1} onTruncate={(labelTruncated) => setLabelTruncated(labelTruncated)}>
            {label}
          </Truncate>
        </span>
      ) : (
        label
      )}
      {hasDescription && <InfoIcon />}
    </span>
  )
}

CodeFieldItemLabel.defaultProps = {
  singleLine: false,
}

export default CodeFieldItemLabel
