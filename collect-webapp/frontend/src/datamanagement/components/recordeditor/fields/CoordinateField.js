import React from 'react'
import { Input } from 'reactstrap'
import { CoordinateAttributeDefinition } from 'model/Survey'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import Objects from 'utils/Objects'
import L from 'utils/Labels'

import AbstractSingleAttributeField from './AbstractSingleAttributeField'
import CompositeAttributeFormItem from './CompositeAttributeFormItem'
import { COORDINATE_FIELD_WIDTH_PX } from './FieldsSizes'

export default class CoordinateField extends AbstractSingleAttributeField {
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
    this.onAttributeUpdate({ value })
  }

  onChangeNumericField({ field, event }) {
    const value = event.target.value
    this.onChangeField({ field, fieldValue: value === '' ? null : Number(value) })
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
    const { srs } = value || {}
    const { attributeDefinition } = fieldDef
    const { availableFieldNames } = attributeDefinition

    const numericField = ({ field }) => (
      <Input
        key={field}
        value={Objects.getProp(field, '')(value)}
        type="number"
        onChange={(event) => this.onChangeNumericField({ field, event })}
        style={{ width: COORDINATE_FIELD_WIDTH_PX }}
      />
    )

    const inputFields = availableFieldNames.map((field) => {
      if (field === CoordinateAttributeDefinition.Fields.SRS) {
        const srss = this.getSpatialReferenceSystems()

        return srss.length === 1 ? (
          <Input key="srs" value={srss[0].label} readOnly style={{ width: COORDINATE_FIELD_WIDTH_PX }} />
        ) : (
          <Input
            key="srs"
            value={srs}
            type="select"
            onChange={this.onChangeSrs}
            style={{ width: COORDINATE_FIELD_WIDTH_PX }}
          >
            {[
              <option key="empty" value="">
                Select...
              </option>,
              ...srss.map((srs) => (
                <option key={srs.id} value={srs.id}>
                  {srs.label}
                </option>
              )),
            ]}
          </Input>
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
      <div style={{ display: inTable ? 'flex' : 'block' }}>
        {inTable
          ? inputFields
          : availableFieldNames.map((field, index) => getFormItem({ field, inputField: inputFields[index] }))}
        {dirty && <LoadingSpinnerSmall />}
      </div>
    )
  }
}
