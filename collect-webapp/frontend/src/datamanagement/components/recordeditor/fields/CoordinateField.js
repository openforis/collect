import React from 'react'
import { MenuItem, Select, TextField as MuiTextField } from '@material-ui/core'
import { CoordinateAttributeDefinition } from 'model/Survey'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import L from 'utils/Labels'
import Objects from 'utils/Objects'
import Numbers from 'utils/Numbers'

import AbstractField from './AbstractField'
import CompositeAttributeFormItem from './CompositeAttributeFormItem'
import { COORDINATE_FIELD_WIDTH_PX } from './FieldsSizes'

export default class CoordinateField extends AbstractField {
  constructor() {
    super()
    this.onChangeSrs = this.onChangeSrs.bind(this)
    this.onChangeNumericField = this.onChangeNumericField.bind(this)
  }

  onChangeField({ field, fieldValue }) {
    const { value: valueState } = this.state
    const { fieldDef } = this.props
    const { showSrsField } = fieldDef.attributeDefinition
    const value = { ...valueState, [field]: fieldValue }
    const { x = null, y = null, srs = null } = value

    const srss = this.getSpatialReferenceSystems()
    if (srss.length === 1 || !showSrsField) {
      if (x !== null && y !== null && srs === null) {
        // Set default SRS (1st one)
        value['srs'] = srss[0].id
      } else if (x === null && y === null) {
        // SRS field is readonly, reset it
        value['srs'] = null
      }
    }
    this.updateValue({ value })
  }

  onChangeNumericField({ field, event }) {
    this.onChangeField({ field, fieldValue: Numbers.toNumber(event.target.value) })
  }

  onChangeSrs(event) {
    this.onChangeField({ field: 'srs', fieldValue: event.target.value })
  }

  getSpatialReferenceSystems() {
    return this.props.parentEntity.definition.survey.spatialReferenceSystems
  }

  render() {
    const { inTable, fieldDef } = this.props
    const { dirty, value } = this.state
    const { srs = '' } = value || {}
    const { attributeDefinition } = fieldDef
    const { availableFieldNames, calculated } = attributeDefinition

    const numericField = ({ field }) => (
      <MuiTextField
        key={field}
        value={Objects.getProp(field, '')(value)}
        type="number"
        variant="outlined"
        disabled={calculated}
        onChange={(event) => this.onChangeNumericField({ field, event })}
        style={{ width: COORDINATE_FIELD_WIDTH_PX }}
      />
    )

    const inputFields = availableFieldNames.map((field) => {
      if (field === CoordinateAttributeDefinition.Fields.SRS) {
        const srss = this.getSpatialReferenceSystems()
        const style = { width: COORDINATE_FIELD_WIDTH_PX }

        return srss.length === 1 ? (
          <MuiTextField key="srs" variant="outlined" value={srss[0].label} readOnly style={style} />
        ) : (
          <Select
            key="srs"
            value={srs}
            variant="outlined"
            disabled={calculated}
            onChange={this.onChangeSrs}
            style={style}
          >
            <MenuItem key="empty" value="">
              <em>{L.l('common.selectOne')}</em>
            </MenuItem>
            {srss.map((srs) => (
              <MenuItem key={srs.id} value={srs.id}>
                {srs.label}
              </MenuItem>
            ))}
          </Select>
        )
      }
      return numericField({ field })
    })

    const getFormItem = ({ field, inputField }) => (
      <CompositeAttributeFormItem
        key={field}
        field={field}
        label={
          attributeDefinition.getFieldLabel(field) || L.l(`dataManagement.dataEntry.attribute.coordinate.${field}`)
        }
        labelWidth={100}
        inputField={inputField}
      />
    )

    return (
      <div style={{ flexDirection: inTable ? 'row' : 'column' }} className="form-item-composite-wrapper">
        {inTable
          ? inputFields
          : availableFieldNames.map((field, index) => getFormItem({ field, inputField: inputFields[index] }))}
        {dirty && <LoadingSpinnerSmall />}
      </div>
    )
  }
}
