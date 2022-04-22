import React from 'react'

import PropTypes from 'prop-types'

import moment from 'moment'

import { TextField } from '@mui/material'

import { DatePicker as MuiDatePicker } from '@mui/x-date-pickers/DatePicker'
import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'

import Dates from 'utils/Dates'

export const DatePicker = (props) => {
  const { disabled, maxDate: maxDateProp, minDate: minDateProp, onChange, style, value } = props

  const minDate = Dates.parseDateISO('2021-01-01')
  const maxDate = Dates.parseDateISO('2023-01-01')

  return (
    <LocalizationProvider dateAdapter={AdapterMoment}>
      <MuiDatePicker
        allowSameDateSelection
        disabled={disabled}
        inputFormat={Dates.DATE_FORMAT}
        minDate={minDate ? moment(minDate) : undefined}
        maxDate={maxDate ? moment(maxDate) : undefined}
        onChange={(momentObj) => onChange(momentObj?.toDate())}
        renderInput={(params) => <TextField {...params} />}
        style={style}
        value={value}
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
