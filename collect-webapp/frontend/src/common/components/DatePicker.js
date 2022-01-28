import React from 'react'
import PropTypes from 'prop-types'
import { KeyboardDatePicker, MuiPickersUtilsProvider } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

import Dates from 'utils/Dates'

export const DatePicker = (props) => {
  const { disabled, maxDate, minDate, onChange, style, value } = props

  return (
    <MuiPickersUtilsProvider utils={DateFnsUtils}>
      <KeyboardDatePicker
        disabled={disabled}
        variant="dialog"
        inputVariant="outlined"
        format={Dates.DATE_FORMAT}
        margin="none"
        minDate={minDate}
        maxDate={maxDate}
        value={value}
        onChange={onChange}
        style={style}
      />
    </MuiPickersUtilsProvider>
  )
}

DatePicker.propTypes = {
  disabled: PropTypes.bool,
  maxDate: PropTypes.oneOfType([PropTypes.object, PropTypes.string, PropTypes.number]),
  minDate: PropTypes.oneOfType([PropTypes.object, PropTypes.string, PropTypes.number]),
  onChange: PropTypes.func,
  style: PropTypes.object,
  value: PropTypes.oneOfType([PropTypes.object, PropTypes.string, PropTypes.number]),
}

DatePicker.defaultProps = {
  disabled: false,
  maxDate: null,
  minDate: null,
  onChange: null,
  style: null,
  value: null,
}
