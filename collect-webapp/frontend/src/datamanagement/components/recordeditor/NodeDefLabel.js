import React from 'react'
import PropTypes from 'prop-types'
import classNames from 'classnames'
import { Label } from 'reactstrap'

import InfoIcon from 'common/components/InfoIcon'
import { NodeDefinition } from 'model/Survey'

const NodeDefLabel = (props) => {
  const { nodeDefinition, limitWidth } = props
  const { numberLabel, labelOrName, description, labelWidth: labelWidthNodeDef } = nodeDefinition

  const hasNumberLabel = !!numberLabel
  const style = limitWidth ? { width: `${Math.max(labelWidthNodeDef || 0, 200)}px` } : {}

  return (
    <div className={classNames('node-def-label-wrapper', { 'with-number': hasNumberLabel })} style={style}>
      {hasNumberLabel && (
        <Label className="number-label">
          {numberLabel}
          {NodeDefinition.NUMBER_LABEL_SUFFIX}
        </Label>
      )}
      <Label title={description}>
        {labelOrName}
        {description && <InfoIcon />}
      </Label>
    </div>
  )
}

NodeDefLabel.propTypes = {
  nodeDefinition: PropTypes.instanceOf(NodeDefinition).isRequired,
  limitWidth: PropTypes.bool,
}

NodeDefLabel.defaultProps = {
  limitWidth: true,
}

export default NodeDefLabel
