import './NumberField.scss'

import React from 'react'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import InputNumber from 'common/components/InputNumber'

import UnitField from './UnitField'
import AbstractNumericField from './AbstractNumericField'
import Numbers from 'utils/Numbers'

const UNIT_FIELD_WIDTH = 80

export default class NumberField extends AbstractNumericField {
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
    const { fieldDef, inTable, parentEntity } = this.props
    const { dirty, value: valueState } = this.state
    const { record } = parentEntity
    const { value, unit: unitId } = valueState || {}
    const { attributeDefinition } = fieldDef

    const readOnly = record.readOnly || attributeDefinition.calculated

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
        {dirty && <LoadingSpinnerSmall />}
      </>
    )
  }
}
