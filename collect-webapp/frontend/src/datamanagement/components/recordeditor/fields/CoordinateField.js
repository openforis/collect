import React from 'react'
import { Input, Label } from 'reactstrap'
import { CoordinateAttributeDefinition } from '../../../../model/Survey'

import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'
import CompositeAttributeFormItem from './CompositeAttributeFormItem'

const numericField = ({ value, onChange }) => <Input value={value} type="number" onChange={onChange} />

export default class CoordinateField extends AbstractField {
  constructor() {
    super()

    this.onChangeX = this.onChangeX.bind(this)
    this.onChangeY = this.onChangeY.bind(this)
    this.onChangeSrs = this.onChangeSrs.bind(this)
    this.onChangeAltitude = this.onChangeAltitude.bind(this)
    this.onChangeAccuracy = this.onChangeAccuracy.bind(this)
  }

  onChangeField({ fieldName, fieldValue }) {
    const { value: valueState } = this.state
    const { fieldDef } = this.props
    const { showSrsField } = fieldDef.attributeDefinition
    const value = { ...valueState, [fieldName]: fieldValue }
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

  onChangeNumericField({ fieldName, event }) {
    const value = event.target.value
    this.onChangeField({ fieldName, fieldValue: value === '' ? null : Number(value) })
  }

  onChangeX(event) {
    this.onChangeNumericField({ fieldName: 'x', event })
  }

  onChangeY(event) {
    this.onChangeNumericField({ fieldName: 'y', event })
  }

  onChangeSrs(event) {
    this.onChangeField({ fieldName: 'srs', fieldValue: event.target.value })
  }

  onChangeAltitude(event) {
    this.onChangeNumericField({ fieldName: 'altitude', event })
  }

  onChangeAccuracy(event) {
    this.onChangeNumericField({ fieldName: 'accuracy', event })
  }

  getSpatialReferenceSystems() {
    return this.props.parentEntity.definition.survey.spatialReferenceSystems
  }

  render() {
    const { inTable, fieldDef } = this.props
    const { dirty, value } = this.state
    const { x, y, srs, altitude, accuracy } = value || {}
    const xText = x || ''
    const yText = y || ''
    const altitudeText = altitude || ''
    const accuracyText = accuracy || ''
    const attrDef = fieldDef.attributeDefinition
    const { fieldsOrder, showSrsField, includeAltitudeField, includeAccuracyField } = attrDef

    const srss = this.getSpatialReferenceSystems()
    const srsField = !showSrsField ? null : srss.length === 1 ? (
      <Label>{srss[0].label}</Label>
    ) : (
      <Input value={srs} type="select" onChange={this.onChangeSrs}>
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
    const xField = numericField({ value: xText, onChange: this.onChangeX })
    const yField = numericField({ value: yText, onChange: this.onChangeY })
    const altitudeField = includeAltitudeField
      ? numericField({ value: altitudeText, onChange: this.onChangeAltitude })
      : null
    const accuracyField = includeAccuracyField
      ? numericField({ value: accuracyText, onChange: this.onChangeAccuracy })
      : null

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
        const xFieldLabel = attrDef.getFieldLabel('x') || 'X'
        const yFieldLabel = attrDef.getFieldLabel('y') || 'Y'
        const srsFieldLabel = attrDef.getFieldLabel('srs') || 'SRS'

        const xFormItem = <CompositeAttributeFormItem key="x" field="x" label={xFieldLabel} inputField={xField} />
        const yFormItem = <CompositeAttributeFormItem key="y" field="y" label={yFieldLabel} inputField={yField} />
        const srsFormItem = showSrsField ? (
          <CompositeAttributeFormItem key="srs" field="srs" label={srsFieldLabel} inputField={srsField} />
        ) : null

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
          const altitudeFieldLabel = attrDef.getFieldLabel('altitude') || 'Altitude'
          internalParts.push(
            <CompositeAttributeFormItem
              key="altitude"
              field="altitude"
              label={altitudeFieldLabel}
              inputField={altitudeField}
            />
          )
        }
        if (includeAccuracyField) {
          const accuracyFieldLabel = attrDef.getFieldLabel('accuracy') || 'Accuracy'
          internalParts.push(
            <CompositeAttributeFormItem
              key="accuracy"
              field="accuracy"
              label={accuracyFieldLabel}
              inputField={accuracyField}
            />
          )
        }
      }
      return internalParts
    }
    return (
      <div>
        {getInternalContent()}
        {dirty && <FieldLoadingSpinner />}
      </div>
    )
  }
}
