import React from 'react'
import { MenuItem, Select, TextField as MuiTextField } from '@material-ui/core'
import { CoordinateAttributeDefinition } from 'model/Survey'

import InputNumber from 'common/components/InputNumber'
import L from 'utils/Labels'
import Objects from 'utils/Objects'

import AbstractField from './AbstractField'
import CompositeAttributeFormItem from './CompositeAttributeFormItem'
import { COORDINATE_FIELD_WIDTH_PX } from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'

export default class CoordinateField extends AbstractField {
  constructor() {
    super()
    this.onChangeSrs = this.onChangeSrs.bind(this)
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

  onChangeSrs(event) {
    this.onChangeField({ field: 'srs', fieldValue: event.target.value })
  }

  getSpatialReferenceSystems() {
    return this.props.parentEntity.definition.survey.spatialReferenceSystems
  }

  render() {
    const { inTable, fieldDef, parentEntity } = this.props
    const { dirty, value } = this.state
    const { record } = parentEntity
    const { srs = '' } = value || {}
    const { attributeDefinition } = fieldDef
    const { availableFieldNames, calculated } = attributeDefinition
    const readOnly = record.readOnly || calculated

    const numericField = ({ field }) => (
      <InputNumber
        key={field}
        decimalScale={10}
        value={Objects.getProp(field, '')(value)}
        readOnly={readOnly}
        onChange={(fieldValue) => this.onChangeField({ field, fieldValue })}
        width={COORDINATE_FIELD_WIDTH_PX}
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
            disabled={readOnly}
            onChange={this.onChangeSrs}
            style={style}
          >
            <MenuItem key="empty" value="">
              <em>{L.l('common.selectOne')}</em>
            </MenuItem>
            {srss.map((srs) => {
              const itemLabel = `${srs.label}${srs.label === srs.id ? '' : ` (${srs.id})`}`
              return (
                <MenuItem key={srs.id} value={srs.id}>
                  {itemLabel}
                </MenuItem>
              )
            })}
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
      <>
        <div style={{ flexDirection: inTable ? 'row' : 'column' }} className="form-item-composite-wrapper">
          {inTable
            ? inputFields
            : availableFieldNames.map((field, index) => getFormItem({ field, inputField: inputFields[index] }))}
        </div>
        {dirty && <DirtyFieldSpinner />}
      </>
    )
  }
}
