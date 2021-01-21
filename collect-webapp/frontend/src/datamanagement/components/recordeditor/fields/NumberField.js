import './NumberField.scss'

import React from 'react'
import { connect } from 'react-redux'

import InputNumber from 'common/components/InputNumber'

import UnitField from './UnitField'
import AbstractNumericField from './AbstractNumericField'
import Numbers from 'utils/Numbers'
import DirtyFieldSpinner from './DirtyFieldSpinner'

const UNIT_FIELD_WIDTH = 80

class NumberField extends AbstractNumericField {
  constructor(props) {
    super(props)
    this.onInputValueChange = this.onInputValueChange.bind(this)
  }

  extractValueFromProps() {
    const attr = this.getAttribute()

    if (!attr) {
      return null
    }
    return { value: attr.valueField.value, unit: this.getSelectedUnitIdFromProps() }
  }

  onInputValueChange(inputValue) {
    const { value } = this.state
    const valueUpdated = { ...value, value: inputValue }
    this.updateValue({ value: valueUpdated })
  }

  render() {
    const { fieldDef, inTable, parentEntity, user } = this.props
    const { dirty, value: valueState } = this.state
    const { record } = parentEntity
    const { value, unit: unitId } = valueState || {}
    const { attributeDefinition } = fieldDef

    const readOnly = !user.canEditRecordAttribute({ record, attributeDefinition })

    // keep decimal places of already inserted values (backwards compatibility)
    const decimalScale = Math.max(attributeDefinition.getDecimalScale(unitId), Numbers.countDecimals(value))
    const unitVisible = attributeDefinition.isUnitVisible({ inTable })
    const wrapperStyle = unitVisible ? { display: 'grid', gridTemplateColumns: `1fr ${UNIT_FIELD_WIDTH}px` } : null

    return (
      <>
        <div className="number-field-wrapper" style={wrapperStyle}>
          <InputNumber
            decimalScale={decimalScale}
            maxLength={attributeDefinition.getMaxLength(unitId)}
            value={value}
            readOnly={readOnly}
            onChange={this.onInputValueChange}
          />

          {unitVisible && (
            <UnitField
              attributeDefinition={attributeDefinition}
              onChange={this.onUnitChange}
              unitId={unitId}
              inTable={inTable}
              readOnly={readOnly}
            />
          )}
        </div>
        {dirty && <DirtyFieldSpinner />}
      </>
    )
  }
}

const mapStateToProps = (state) => {
  const { session } = state
  const { loggedUser: user } = session
  return { user }
}

export default connect(mapStateToProps)(NumberField)
