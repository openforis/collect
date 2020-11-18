import React from 'react'
import classNames from 'classnames'
import { FormControl, InputLabel, MenuItem, Select, TextField as MuiTextField } from '@material-ui/core'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import L from 'utils/Labels'
import AbstractField from './AbstractField'

export default class NumberField extends AbstractField {
  constructor(props) {
    super(props)

    this.onTextValueChange = this.onTextValueChange.bind(this)
    this.onUnitChange = this.onUnitChange.bind(this)
  }

  extractValueFromProps() {
    const { fieldDef } = this.props
    const attr = this.getAttribute()

    if (!attr) {
      return null
    }

    const attrDef = fieldDef.attributeDefinition
    const precisions = attrDef.precisions

    const unitId = attr.fields[1].value
    let selectedUnitId
    if (unitId) {
      selectedUnitId = unitId
    } else if (precisions.length) {
      const defaultPrecision = precisions.find((precision) => precision.defaultPrecision) || precisions[0]
      selectedUnitId = defaultPrecision.unitId
    }

    return { value: attr.fields[0].value, unit: selectedUnitId }
  }

  onTextValueChange(event) {
    const { value } = this.state
    const valueUpdated = { ...value, value: Number(event.target.value) }
    this.updateValue({ value: valueUpdated })
  }

  onUnitChange(event) {
    const { value } = this.state
    const valueUpdated = { ...value, unit: Number(event.target.value) }
    this.updateValue({ value: valueUpdated })
  }

  render() {
    const { fieldDef } = this.props
    const { dirty, value: valueState } = this.state
    const { value, unit: unitId } = valueState || {}
    const { attributeDefinition: attrDef } = fieldDef
    const { precisions, calculated } = attrDef
    const text = value || ''
    const hasPrecisions = precisions.length > 0
    const wrapperStyle = hasPrecisions ? { display: 'grid', gridTemplateColumns: '1fr 80px' } : null

    return (
      <>
        <div style={wrapperStyle}>
          <MuiTextField
            variant="outlined"
            type="number"
            value={text}
            className={classNames({ readOnly: calculated })}
            disabled={calculated}
            onChange={this.onTextValueChange}
          />

          {hasPrecisions && (
            <FormControl>
              <InputLabel>{L.l('common.unit')}</InputLabel>
              <Select
                variant="outlined"
                value={unitId}
                disabled={calculated}
                onChange={this.onUnitChange}
                label={L.l('common.unit')}
              >
                {precisions.map((precision) => {
                  const unit = attrDef.survey.units.find((unit) => unit.id === precision.unitId)
                  return (
                    <MenuItem key={unit.id} value={unit.id} title={unit.label}>
                      {unit.abbreviation}
                    </MenuItem>
                  )
                })}
              </Select>
            </FormControl>
          )}
        </div>
        {dirty && <LoadingSpinnerSmall />}
      </>
    )
  }
}
