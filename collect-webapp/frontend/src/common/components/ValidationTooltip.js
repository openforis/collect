import React from 'react'
import { UncontrolledTooltip } from 'reactstrap'
import classnames from 'classnames'
import PropTypes from 'prop-types'

import Validation from 'model/Validation'

const ValidationTooltip = (props) => {
  const { validation, target } = props
  const { errorMessage, warningMessage } = validation

  const className = classnames({ error: validation.hasErrors(), warning: validation.hasWarnings() })

  if (validation.isEmpty()) return null

  return (
    <UncontrolledTooltip target={target} placement="top-start" popperClassName={className}>
      {errorMessage || warningMessage}
    </UncontrolledTooltip>
  )
}

ValidationTooltip.propTypes = {
  validation: PropTypes.instanceOf(Validation),
}

ValidationTooltip.defaultProps = {
  validation: new Validation(),
}

export default ValidationTooltip
