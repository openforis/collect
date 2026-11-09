import React from 'react'

import PropTypes from 'prop-types'

import moment from 'moment'

import { DatePicker as MuiDatePicker } from '@mui/x-date-pickers/DatePicker'
import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'

import Dates from 'utils/Dates'

export const DatePicker = (props) => {
  const { disabled = false, maxDate = null, minDate = null, onChange = null, style = null, value = null } = props

  return (
    <LocalizationProvider dateAdapter={AdapterMoment}>
      <MuiDatePicker
        disabled={disabled}
        format={Dates.DATE_FORMAT}
        minDate={minDate ? moment(minDate) : undefined}
        maxDate={maxDate ? moment(maxDate) : undefined}
        onChange={(momentObj) => onChange(momentObj?.toDate())}
        slotProps={{ textField: { style } }}
        value={value ? moment(value) : null}
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
