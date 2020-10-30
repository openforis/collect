import React from 'react'
import { MuiPickersUtilsProvider, KeyboardDatePicker } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

import Dates from 'utils/Dates'
import AbstractSingleAttributeField from './AbstractSingleAttributeField'
import FieldLoadingSpinner from './FieldLoadingSpinner'

const fromValueToDate = (value) => (value ? new Date(value.year, value.month - 1, value.day) : null)
const fromDateToValue = (date) => {
  const dateUtils = new DateFnsUtils()
  return {
    year: dateUtils.getYear(date),
    month: dateUtils.getMonth(date) + 1,
    day: Number(dateUtils.getDayText(date)),
  }
}

export default class DateField extends AbstractSingleAttributeField {
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
    const selectedDate = fromValueToDate(valueState)

    return (
      <div>
        <MuiPickersUtilsProvider utils={DateFnsUtils}>
          <KeyboardDatePicker
            variant="dialog"
            inputVariant="outlined"
            format={Dates.DATE_FORMAT}
            margin="none"
            value={selectedDate}
            onChange={this.onChange}
            style={{ width: '180px' }}
          />
        </MuiPickersUtilsProvider>
        {dirty && <FieldLoadingSpinner />}
      </div>
    )
  }
}
