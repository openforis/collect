import React from 'react'
import { UncontrolledTooltip } from 'reactstrap'
import classnames from 'classnames'

const FieldValidationTooltip = (props) => {
  const { errors, warnings, target } = props
  const visible = Boolean(errors || warnings)

  const className = classnames({ warning: Boolean(warnings), error: Boolean(errors) })

  return visible ? (
    <UncontrolledTooltip target={target} placement="top-start" popperClassName={className}>
      {errors || warnings}
    </UncontrolledTooltip>
  ) : null
}

export default FieldValidationTooltip
