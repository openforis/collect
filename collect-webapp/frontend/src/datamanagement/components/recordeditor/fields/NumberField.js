import React from 'react'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import InputNumber from 'common/components/InputNumber'

import UnitField from './UnitField'
import AbstractNumericField from './AbstractNumericField'

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
    const { fieldDef, inTable } = this.props
    const { dirty, value: valueState } = this.state
    const { value, unit: unitId } = valueState || {}
    const { attributeDefinition } = fieldDef
    const { calculated } = attributeDefinition
    const unitVisible = attributeDefinition.isUnitVisible({ inTable })
    const wrapperStyle = unitVisible ? { display: 'grid', gridTemplateColumns: '1fr 80px' } : null

    return (
      <>
        <div style={wrapperStyle}>
          <InputNumber
            decimalScale={attributeDefinition.isInteger() ? 0 : 10}
            maxLength={attributeDefinition.isInteger() ? 10 : undefined}
            value={value}
            readOnly={calculated}
            onChange={this.onInputValueChange}
          />

          {unitVisible && (
            <UnitField
              attributeDefinition={attributeDefinition}
              onChange={this.onUnitChange}
              unitId={unitId}
              inTable={inTable}
            />
          )}
        </div>
        {dirty && <LoadingSpinnerSmall />}
      </>
    )
  }
}
