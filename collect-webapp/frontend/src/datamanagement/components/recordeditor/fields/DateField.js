import React from 'react'
import { MuiPickersUtilsProvider, KeyboardDatePicker } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

import Dates from 'utils/Dates'
import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'
import FieldValidationTooltip from './FieldValidationTooltip'

const fromValueToDate = (value) => (value ? new Date(value.year, value.month - 1, value.day) : null)
const fromDateToValue = (date) => {
  const dateUtils = new DateFnsUtils()
  return {
    year: dateUtils.getYear(date),
    month: dateUtils.getMonth(date) + 1,
    day: Number(dateUtils.getDayText(date)),
  }
}

export default class DateField extends AbstractField {
  constructor() {
    super()

    this.fieldId = `date-field-${new Date().getTime()}`

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
    const { dirty, value: valueState, errors, warnings } = this.state
    const selectedDate = fromValueToDate(valueState)

    return (
      <div>
        <MuiPickersUtilsProvider utils={DateFnsUtils}>
          <KeyboardDatePicker
            id={this.fieldId}
            variant="dialog"
            inputVariant="outlined"
            format={Dates.DATE_FORMAT}
            margin="none"
            value={selectedDate}
            onChange={this.onChange}
            className={warnings ? 'warning' : ''}
          />
        </MuiPickersUtilsProvider>
        {dirty && <FieldLoadingSpinner />}
        <FieldValidationTooltip target={this.fieldId} errors={errors} warnings={warnings} />
      </div>
    )
  }
}
