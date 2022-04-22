import React from 'react'
import { connect } from 'react-redux'

import { TextField } from '@mui/material'
import { TimePicker as MuiTimePicker } from '@mui/x-date-pickers/TimePicker'
import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'

import Dates from 'utils/Dates'
import AbstractField from './AbstractField'
import * as FieldSizes from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'

const fromValueToDate = (value) => (value ? new Date(1970, 1, 1, value.hour, value.minute) : null)
const fromDateToValue = (date) => ({
  hour: Dates.getHours(date),
  minute: Dates.getMinutes(date),
})

class TimeField extends AbstractField {
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
    const { fieldDef, inTable, parentEntity, user } = this.props
    const { dirty, value: valueState } = this.state
    const { record } = parentEntity
    const { attributeDefinition } = fieldDef
    const readOnly = !user.canEditRecordAttribute({ record, attributeDefinition })

    const selectedTime = fromValueToDate(valueState)
    const width = FieldSizes.getWidth({ fieldDef, inTable })

    return (
      <div>
        <LocalizationProvider dateAdapter={AdapterMoment}>
          <MuiTimePicker
            inputFormat={Dates.TIME_FORMAT}
            ampm={false}
            disabled={readOnly}
            value={selectedTime}
            onChange={this.onChange}
            style={{ width: `${width}px` }}
            renderInput={(params) => <TextField {...params} />}
          />
        </LocalizationProvider>
        {dirty && <DirtyFieldSpinner />}
      </div>
    )
  }
}

const mapStateToProps = (state) => {
  const { session } = state
  const { loggedUser: user } = session
  return { user }
}

export default connect(mapStateToProps)(TimeField)
