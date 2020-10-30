import React from 'react'
import { MuiPickersUtilsProvider, KeyboardTimePicker } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

import Dates from 'utils/Dates'
import AbstractSingleAttributeField from './AbstractSingleAttributeField'
import FieldLoadingSpinner from './FieldLoadingSpinner'

const fromValueToDate = (value) => (value ? new Date(1970, 1, 1, value.hour, value.minute) : null)
const fromDateToValue = (date) => {
  const dateUtils = new DateFnsUtils()
  return {
    hour: dateUtils.getHours(date),
    minute: dateUtils.getMinutes(date),
  }
}

export default class TimeField extends AbstractSingleAttributeField {
  constructor() {
    super()
    this.onChange = this.onChange.bind(this)
  }

  onChange(date) {
    if (isNaN(date)) {
      return
    }
    const value = date === null ? null : fromDateToValue(date)
    this.onAttributeUpdate({ value })
  }

  render() {
    const { dirty, value: valueState } = this.state
    const selectedTime = fromValueToDate(valueState)

    return (
      <div>
        <MuiPickersUtilsProvider utils={DateFnsUtils}>
          <KeyboardTimePicker
            variant="dialog"
            inputVariant="outlined"
            format={Dates.TIME_FORMAT}
            ampm={false}
            margin="none"
            value={selectedTime}
            onChange={this.onChange}
            keyboardIcon={<span className="far fa-clock" />}
            style={{ width: '130px' }}
          />
        </MuiPickersUtilsProvider>
        {dirty && <FieldLoadingSpinner />}
      </div>
    )
  }
}
