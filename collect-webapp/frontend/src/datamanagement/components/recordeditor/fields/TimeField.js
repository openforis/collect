import React from 'react'
import { connect } from 'react-redux'
import { TimePicker as MuiTimePicker } from '@mui/x-date-pickers/TimePicker'
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import DateFnsUtils from '@date-io/date-fns'

import Dates from 'utils/Dates'
import AbstractField from './AbstractField'
import * as FieldSizes from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'

const fromValueToDate = (value) => (value ? new Date(1970, 1, 1, value.hour, value.minute) : null)
const fromDateToValue = (date) => {
  const dateUtils = new DateFnsUtils()
  return {
    hour: dateUtils.getHours(date),
    minute: dateUtils.getMinutes(date),
  }
}

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
        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <MuiTimePicker
            format={Dates.TIME_FORMAT}
            ampm={false}
            margin="none"
            disabled={readOnly}
            value={selectedTime}
            onChange={this.onChange}
            keyboardIcon={<span className="far fa-clock" />}
            style={{ width: `${width}px` }}
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
