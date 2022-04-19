import React from 'react'
import PropTypes from 'prop-types'
import { TextField } from '@mui/material'
import { DatePicker as MuiDatePicker } from '@mui/x-date-pickers/DatePicker'
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'

import Dates from 'utils/Dates'

export const DatePicker = (props) => {
  const { disabled, maxDate, minDate, onChange, style, value } = props

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <MuiDatePicker
        disabled={disabled}
        format={Dates.DATE_FORMAT}
        margin="none"
        minDate={minDate}
        maxDate={maxDate}
        value={value}
        onChange={onChange}
        style={style}
        renderInput={(params) => <TextField {...params} />}
      />
    </LocalizationProvider>
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
