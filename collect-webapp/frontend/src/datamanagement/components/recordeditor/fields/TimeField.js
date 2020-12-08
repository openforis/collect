import React from 'react'
import { MuiPickersUtilsProvider, KeyboardTimePicker } from '@material-ui/pickers'
import DateFnsUtils from '@date-io/date-fns'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import Dates from 'utils/Dates'
import AbstractField from './AbstractField'
import * as FieldSizes from './FieldsSizes'

const fromValueToDate = (value) => (value ? new Date(1970, 1, 1, value.hour, value.minute) : null)
const fromDateToValue = (date) => {
  const dateUtils = new DateFnsUtils()
  return {
    hour: dateUtils.getHours(date),
    minute: dateUtils.getMinutes(date),
  }
}

export default class TimeField extends AbstractField {
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

    const selectedTime = fromValueToDate(valueState)
    const width = FieldSizes.getWidth({ fieldDef, inTable })

    return (
      <div>
        <MuiPickersUtilsProvider utils={DateFnsUtils}>
          <KeyboardTimePicker
            variant="dialog"
            inputVariant="outlined"
            format={Dates.TIME_FORMAT}
            ampm={false}
            margin="none"
            disabled={readOnly}
            value={selectedTime}
            onChange={this.onChange}
            keyboardIcon={<span className="far fa-clock" />}
            style={{ width: `${width}px` }}
          />
        </MuiPickersUtilsProvider>
        {dirty && <LoadingSpinnerSmall />}
      </div>
    )
  }
}
