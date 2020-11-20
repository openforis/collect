import React from 'react'
import classNames from 'classnames'
import { TextField as MuiTextField } from '@material-ui/core'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import L from 'utils/Labels'
import Objects from 'utils/Objects'

import AbstractNumericField from './AbstractNumericField'
import UnitField from './UnitField'
import * as FieldsSizes from './FieldsSizes'

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
    const { fieldDef, inTable } = this.props
    const { dirty, value = {} } = this.state
    const { unit: unitId } = value || {}
    const { attributeDefinition } = fieldDef
    const { calculated } = attributeDefinition
    const unitVisible = attributeDefinition.isUnitVisible({ inTable })
    const wrapperStyle = {
      display: 'grid',
      gridTemplateColumns: `repeat(2, 0.5fr) ${unitVisible ? '80px' : ''}`,
      width: FieldsSizes.getWidthPx({ fieldDef, inTable }),
    }

    const getInputField = ({ fieldName }) => (
      <MuiTextField
        variant="outlined"
        type="number"
        label={L.l(`dataManagement.dataEntry.attribute.range.${fieldName}`)}
        value={Objects.getProp(fieldName, '')(value)}
        className={classNames({ readOnly: calculated })}
        disabled={calculated}
        onChange={(event) => this.onInputValueChange({ fieldName, fieldValue: Number(event.target.value) })}
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
            />
          )}
        </div>
        {dirty && <LoadingSpinnerSmall />}
      </>
    )
  }
}