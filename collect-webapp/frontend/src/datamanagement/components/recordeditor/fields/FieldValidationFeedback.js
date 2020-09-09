import React from 'react'
import classnames from 'classnames'
import { FormFeedback } from 'reactstrap'

export default (props) => {
  const { errors, warnings } = props
  const visible = Boolean(errors || warnings)

  return visible ? (
    <FormFeedback className={classnames({ warning: Boolean(warnings) })}>{errors || warnings}</FormFeedback>
  ) : null
}
