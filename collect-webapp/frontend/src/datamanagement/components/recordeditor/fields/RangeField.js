import React from 'react'

import InputNumber from 'common/components/InputNumber'
import L from 'utils/Labels'
import Objects from 'utils/Objects'

import AbstractNumericField from './AbstractNumericField'
import UnitField from './UnitField'
import * as FieldsSizes from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'

export default class RangeField extends AbstractNumericField {
  constructor(props) {
    super(props)

    this.onInputValueChange = this.onInputValueChange.bind(this)
  }

  extractValueFromProps() {
    const attr = this.getAttribute()
    if (!attr) {
      return null
    }
    return { from: attr.fromField.value, to: attr.toField.value, unit: this.getSelectedUnitIdFromProps() }
  }

  onInputValueChange({ fieldName, fieldValue }) {
    const { value } = this.state
    const valueUpdated = { ...value, [fieldName]: fieldValue }
    this.updateValue({ value: valueUpdated })
  }

  render() {
    const { fieldDef, inTable, parentEntity } = this.props
    const { dirty, value = {} } = this.state
    const { record } = parentEntity
    const { unit: unitId } = value || {}
    const { attributeDefinition } = fieldDef
    const readOnly = record.readOnly || attributeDefinition.calculated

    const unitVisible = attributeDefinition.isUnitVisible({ inTable })
    const wrapperStyle = {
      display: 'grid',
      gridTemplateColumns: `repeat(2, 0.5fr) ${unitVisible ? '80px' : ''}`,
      width: FieldsSizes.getWidthPx({ fieldDef, inTable }),
    }

    const getInputField = ({ fieldName }) => (
      <InputNumber
        decimalScale={attributeDefinition.getDecimalScale(unitId)}
        maxLength={attributeDefinition.getMaxLength(unitId)}
        value={Objects.getProp(fieldName, '')(value)}
        readOnly={readOnly}
        onChange={(fieldValue) => this.onInputValueChange({ fieldName, fieldValue })}
        label={L.l(`dataManagement.dataEntry.attribute.range.${fieldName}`)}
      />
    )

    return (
      <>
        <div style={wrapperStyle}>
          {getInputField({ fieldName: 'from' })}
          {getInputField({ fieldName: 'to' })}
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
