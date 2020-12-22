import React from 'react'
import { MuiPickersUtilsProvider, KeyboardDatePicker } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

import Dates from 'utils/Dates'
import AbstractField from './AbstractField'
import * as FieldSizes from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'

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

  onChange(date) {
    if (isNaN(date)) {
      return
    }
    const value = date === null ? null : fromDateToValue(date)
    this.updateValue({ value })
  }

  render() {
    const { fieldDef, inTable, parentEntity } = this.props
    const { dirty, value: valueState } = this.state
    const { record } = parentEntity
    const { attributeDefinition } = fieldDef
    const readOnly = record.readOnly || attributeDefinition.calculated

    const selectedDate = fromValueToDate(valueState)

    return (
      <>
        <MuiPickersUtilsProvider utils={DateFnsUtils}>
          <KeyboardDatePicker
            variant="dialog"
            inputVariant="outlined"
            format={Dates.DATE_FORMAT}
            margin="none"
            value={selectedDate}
            disabled={readOnly}
            onChange={this.onChange}
            style={{ width: `${FieldSizes.getWidth({ fieldDef, inTable })}px` }}
          />
        </MuiPickersUtilsProvider>
        {dirty && <DirtyFieldSpinner />}
      </>
    )
  }
}
