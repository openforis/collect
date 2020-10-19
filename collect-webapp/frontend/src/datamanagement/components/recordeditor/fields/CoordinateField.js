import React from 'react'
import { Col, Input, Label, Row } from 'reactstrap'
import { CoordinateAttributeDefinition } from '../../../../model/Survey'

import AbstractField from './AbstractField'
import FieldLoadingSpinner from './FieldLoadingSpinner'
import ValidationTooltip from 'common/components/ValidationTooltip'

const numericField = ({ value, onChange, errors, warnings }) => (
  <Input
    invalid={Boolean(errors || warnings)}
    className={warnings ? 'warning' : ''}
    value={value}
    type="number"
    onChange={onChange}
  />
)

const formItem = ({ field, fieldLabel, inputField }) => (
  <Row key={field}>
    <Col style={{ maxWidth: '50px' }}>
      <Label>{fieldLabel}</Label>
    </Col>
    <Col>{inputField}</Col>
  </Row>
)

export default class CoordinateField extends AbstractField {
  constructor() {
    super()

    this.wrapperId = `coordinate-field-${new Date().getTime()}`

    this.onChangeX = this.onChangeX.bind(this)
    this.onChangeY = this.onChangeY.bind(this)
    this.onChangeSrs = this.onChangeSrs.bind(this)
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

  onChangeAltitude(event) {
    this.onChangeNumericField({ fieldName: 'altitude', event })
  }

  onChangeAccuracy(event) {
    this.onChangeNumericField({ fieldName: 'accuracy', event })
  }

  onChangeSrs(event) {
    this.onChangeField({ fieldName: 'srs', fieldValue: event.target.value })
  }

  getSpatialReferenceSystems() {
    return this.props.parentEntity.definition.survey.spatialReferenceSystems
  }

  render() {
    const { inTable, fieldDef } = this.props
    const { dirty, value, errors, warnings } = this.state
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
      <Input
        invalid={Boolean(errors || warnings)}
        className={warnings ? 'warning' : ''}
        value={srs}
        type="select"
        onChange={this.onChangeSrs}
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
    const xField = numericField({ value: xText, onChange: this.onChangeX, errors, warnings })
    const yField = numericField({ value: yText, onChange: this.onChangeY, errors, warnings })
    const altitudeField = includeAltitudeField
      ? numericField({ value: altitudeText, onChange: this.onChangeAltitude, errors, warnings })
      : null
    const accuracyField = includeAccuracyField
      ? numericField({ value: accuracyText, onChange: this.onChangeAccuracy, errors, warnings })
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

        const xFormItem = formItem({ field: 'x', fieldLabel: xFieldLabel, inputField: xField })
        const yFormItem = formItem({ field: 'y', fieldLabel: yFieldLabel, inputField: yField })
        const srsFormItem = showSrsField
          ? formItem({ field: 'srs', fieldLabel: srsFieldLabel, inputField: srsField })
          : null

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
        }
        if (includeAltitudeField) {
          const altitudeFieldLabel = attrDef.getFieldLabel('altitude') || 'Altitude'
          internalParts.push(formItem({ field: 'altitude', fieldLabel: altitudeFieldLabel, inputField: altitudeField }))
        }
        if (includeAccuracyField) {
          const accuracyFieldLabel = attrDef.getFieldLabel('accuracy') || 'Accuracy'
          internalParts.push(formItem({ field: 'accuracy', fieldLabel: accuracyFieldLabel, inputField: accuracyField }))
        }
      }
      return internalParts
    }
    return (
      <div id={this.wrapperId}>
        {getInternalContent()}
        <ValidationTooltip target={this.wrapperId} errors={errors} warnings={warnings} />
        {dirty && <FieldLoadingSpinner />}
      </div>
    )
  }
}
