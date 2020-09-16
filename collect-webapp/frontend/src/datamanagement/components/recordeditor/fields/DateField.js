import React from 'react'
import { MuiPickersUtilsProvider, KeyboardDatePicker } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

import Dates from 'utils/Dates'
import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'
import FieldValidationFeedback from './FieldValidationFeedback'

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

    this.onChange = this.onChange.bind(this)
  }

  extractValueFromProps() {
    const attr = this.getSingleAttribute()
    const value = attr.empty
      ? null
      : attr.definition.fieldNames.reduce((valueAcc, fieldName, index) => {
          valueAcc[fieldName] = attr.fields[index].value
          return valueAcc
        }, {})
    return value
  }

  onChange(date) {
    if (isNaN(date)) {
      return
    }
    this.onAttributeUpdate({
      value: fromDateToValue(date),
    })
  }

  render() {
    const { dirty, value: valueState, errors, warnings } = this.state
    const selectedDate = fromValueToDate(valueState)

    return (
      <div>
        <>
          <MuiPickersUtilsProvider utils={DateFnsUtils}>
            <KeyboardDatePicker
              variant="dialog"
              format={Dates.DATE_FORMAT}
              margin="normal"
              value={selectedDate}
              onChange={this.onChange}
              className={warnings ? 'warning' : ''}
            />
          </MuiPickersUtilsProvider>
          {dirty && <FieldLoadingSpinner />}
        </>
        <FieldValidationFeedback errors={errors} warnings={warnings} />
      </div>
    )
  }
}
