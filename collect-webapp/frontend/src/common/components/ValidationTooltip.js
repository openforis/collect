import React from 'react'
import { UncontrolledTooltip } from 'reactstrap'
import classnames from 'classnames'
import PropTypes from 'prop-types'

const ValidationTooltip = (props) => {
  const { errors, warnings, target } = props
  const visible = Boolean(errors || warnings)

  const className = classnames({ warning: Boolean(warnings), error: Boolean(errors) })

  return visible ? (
    <UncontrolledTooltip target={target} placement="top-start" popperClassName={className}>
      {errors || warnings}
    </UncontrolledTooltip>
  ) : null
}

ValidationTooltip.propTypes = {
  errors: PropTypes.string,
  warnings: PropTypes.string,
}

ValidationTooltip.defaultProps = {
  errors: null,
  warnings: null,
}

export default ValidationTooltip
