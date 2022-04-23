import React from 'react'
import { connect } from 'react-redux'
import classNames from 'classnames'

import Dates from 'utils/Dates'
import L from 'utils/Labels'
import { DatePicker } from 'common/components/DatePicker'

import AbstractField from './AbstractField'
import * as FieldSizes from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'

const fromValueToDate = (value) => (value ? new Date(value.year, value.month - 1, value.day) : null)
const fromDateToValue = (date) => ({
  year: Dates.getYear(date),
  month: Dates.getMonth(date) + 1,
  day: Dates.getDay(date),
})

class DateField extends AbstractField {
  constructor() {
    super()
    this.datePickerWrapperRef = React.createRef()
    this.onChange = this.onChange.bind(this)
  }

  onChange(date) {
    const incomplete = isNaN(date)

    this.handleIncompleteFeedback(incomplete)

    if (incomplete) {
      return
    }
    const value = date === null ? null : fromDateToValue(date)
    this.updateValue({ value })
  }

  handleIncompleteFeedback(incomplete) {
    const wrapper = this.datePickerWrapperRef.current
    wrapper.className = classNames('date-picker-wrapper', { error: incomplete })
    wrapper.title = incomplete ? L.l('dataManagement.dataEntry.attribute.date.incompleteDate') : ''
  }

  render() {
    const { fieldDef, inTable, parentEntity, user } = this.props
    const { dirty, value: valueState } = this.state
    const { record } = parentEntity
    const { attributeDefinition } = fieldDef

    const readOnly = !user.canEditRecordAttribute({ record, attributeDefinition })

    const selectedDate = fromValueToDate(valueState)

    return (
      <div ref={this.datePickerWrapperRef}>
        <DatePicker
          value={selectedDate}
          onChange={this.onChange}
          disabled={readOnly}
          style={{ width: `${FieldSizes.getWidth({ fieldDef, inTable })}px` }}
        />
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

export default connect(mapStateToProps)(DateField)
