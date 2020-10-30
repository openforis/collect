import React from 'react'
import { Input, Label } from 'reactstrap'
import { CoordinateAttributeDefinition } from '../../../../model/Survey'

import AbstractSingleAttributeField from './AbstractSingleAttributeField'
import FieldLoadingSpinner from './FieldLoadingSpinner'
import CompositeAttributeFormItem from './CompositeAttributeFormItem'
import { COORDINATE_FIELD_WIDTH_PX } from './FieldsSizes'
import Objects from 'utils/Objects'
import L from 'utils/Labels'

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
    const attrDef = fieldDef.attributeDefinition

    const { fieldsOrder, showSrsField, includeAltitudeField, includeAccuracyField } = attrDef

    const srss = this.getSpatialReferenceSystems()
    const srsField = !showSrsField ? null : srss.length === 1 ? (
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

    const numericField = ({ field }) => (
      <Input
        key={field}
        value={Objects.getProp(field, '')(value)}
        type="number"
        onChange={(event) => this.onChangeNumericField({ field, event })}
        style={{ width: COORDINATE_FIELD_WIDTH_PX }}
      />
    )
    const xField = numericField({ field: 'x' })
    const yField = numericField({ field: 'y' })
    const altitudeField = includeAltitudeField ? numericField({ field: 'altitude' }) : null
    const accuracyField = includeAccuracyField ? numericField({ field: 'accuracy' }) : null

    const getInternalContent = () => {
      let internalParts = null
      if (inTable) {
        switch (fieldsOrder) {
          case CoordinateAttributeDefinition.FieldsOrder.SRS_X_Y:
            internalParts = [srsField, xField, yField]
            break
          case CoordinateAttributeDefinition.FieldsOrder.SRS_Y_X:
            internalParts = [srsField, yField, xField]
            break
          case CoordinateAttributeDefinition.FieldsOrder.X_Y_SRS:
            internalParts = [xField, yField, srsField]
            break
          case CoordinateAttributeDefinition.FieldsOrder.Y_X_SRS:
            internalParts = [yField, xField, srsField]
            break
          default:
            throw new Error(`Fields order not supported: ${fieldsOrder}`)
        }
        if (includeAltitudeField) {
          internalParts.push(altitudeField)
        }
        if (includeAccuracyField) {
          internalParts.push(accuracyField)
        }
      } else {
        const getFormItem = ({ field, inputField }) => (
          <CompositeAttributeFormItem
            key={field}
            field={field}
            label={attrDef.getFieldLabel(field) || L.l(`dataManagement.dataEntry.coordinateField.${field}`)}
            labelWidth={100}
            inputField={inputField}
          />
        )
        const xFormItem = getFormItem({ field: 'x', inputField: xField })
        const yFormItem = getFormItem({ field: 'y', inputField: yField })
        const srsFormItem = showSrsField ? getFormItem({ field: 'srs', inputField: srsField }) : null

        switch (attrDef.fieldsOrder) {
          case CoordinateAttributeDefinition.FieldsOrder.SRS_X_Y:
            internalParts = [srsFormItem, xFormItem, yFormItem]
            break
          case CoordinateAttributeDefinition.FieldsOrder.SRS_Y_X:
            internalParts = [srsFormItem, yFormItem, xFormItem]
            break
          case CoordinateAttributeDefinition.FieldsOrder.X_Y_SRS:
            internalParts = [xFormItem, yFormItem, srsFormItem]
            break
          case CoordinateAttributeDefinition.FieldsOrder.Y_X_SRS:
            internalParts = [yFormItem, xFormItem, srsFormItem]
            break
          default:
            throw new Error(`Fields order not supported: ${fieldsOrder}`)
        }
        if (includeAltitudeField) {
          internalParts.push(getFormItem({ field: 'altitude', inputField: altitudeField }))
        }
        if (includeAccuracyField) {
          internalParts.push(getFormItem({ field: 'accuracy', inputField: accuracyField }))
        }
      }
      return internalParts
    }
    return (
      <div style={{ display: inTable ? 'flex' : 'block' }}>
        {getInternalContent()}
        {dirty && <FieldLoadingSpinner />}
      </div>
    )
  }
}
