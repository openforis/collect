import './CoordinateField.scss'

import React from 'react'
import { connect } from 'react-redux'
import { MenuItem, Select, TextField as MuiTextField, IconButton } from '@mui/material'
import { MyLocation } from '@mui/icons-material'

import { CoordinateAttributeDefinition } from 'model/Survey'

import Dialogs from 'common/components/Dialogs'
import InputNumber from 'common/components/InputNumber'

import BrowserUtils from 'utils/BrowserUtils'
import L from 'utils/Labels'
import Objects from 'utils/Objects'

import AbstractField from './AbstractField'
import CompositeAttributeFormItem from './CompositeAttributeFormItem'
import { COORDINATE_FIELD_WIDTH_PX } from './FieldsSizes'
import DirtyFieldSpinner from './DirtyFieldSpinner'
import ServiceFactory from 'services/ServiceFactory'

const latLonSrsId = 'EPSG:4326'

class CoordinateField extends AbstractField {
  constructor() {
    super()
    this.onChangeFields = this.onChangeFields.bind(this)
    this.onChangeSrs = this.onChangeSrs.bind(this)
    this.onSetCurrentLocationClick = this.onSetCurrentLocationClick.bind(this)
  }

  getSpatialReferenceSystems() {
    return this.props.parentEntity.survey.spatialReferenceSystems
  }

  getLatLonSrs() {
    return this.getSpatialReferenceSystems().find((srs) => srs.id === latLonSrsId)
  }

  getCurrentSrsIdOrDefault() {
    const { value } = this.state
    const { srs } = value ?? {}
    return srs ?? this.getSpatialReferenceSystems()[0]?.id
  }

  isGeoLocationSupported() {
    return BrowserUtils.isGeoLocationSupported
  }

  onChangeField({ field, fieldValue }) {
    this.onChangeFields({ [field]: fieldValue })
  }

  onChangeFields(fieldValuePairs) {
    const { value: valueState } = this.state
    const { fieldDef } = this.props
    const { showSrsField } = fieldDef.attributeDefinition
    const value = { ...valueState, ...fieldValuePairs }
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

  async onSetCurrentLocationConfirm() {
    try {
      const { latitude, longitude } = await BrowserUtils.getCurrentPosition()
      const currentSrsId = this.getCurrentSrsIdOrDefault()
      const currentLocationCoordinate = { x: longitude, y: latitude, srs: latLonSrsId }
      if (currentSrsId === latLonSrsId) {
        this.onChangeFields(currentLocationCoordinate)
      } else {
        const convertedCoordinate = await ServiceFactory.geoService.convertCoordinate(
          currentLocationCoordinate,
          currentSrsId
        )
        this.onChangeFields(convertedCoordinate)
      }
    } catch (error) {
      this.onErrorGettingCurrentLocation(error)
    }
  }

  onErrorGettingCurrentLocation(error) {
    Dialogs.alert(
      L.l('global.error'),
      L.l('dataManagement.dataEntry.attribute.coordinate.error_getting_current_location', [error.message])
    )
  }

  onSetCurrentLocationClick() {
    const { x, y } = this.state
    if (!!x || !!y) {
      Dialogs.confirm(
        L.l('global.confirm'),
        L.l('dataManagement.dataEntry.attribute.coordinate.confirm_overwrite_coordinate_with_current_location'),
        this.onSetCurrentLocationConfirm
      )
    } else {
      this.onSetCurrentLocationConfirm()
    }
  }

  render() {
    const { inTable, fieldDef, parentEntity, user } = this.props
    const { dirty, value } = this.state
    const { record } = parentEntity
    const { srs = '' } = value || {}
    const { attributeDefinition } = fieldDef
    const { availableFieldNames } = attributeDefinition
    const readOnly = !user.canEditRecordAttribute({ record, attributeDefinition })
    const currentLocationButtonVisible = !readOnly && this.isGeoLocationSupported()

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
          <MuiTextField key="srs" variant="outlined" value={srss[0].label} disabled style={style} />
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
        <div className="coordinate-field-wrapper">
          <div style={{ flexDirection: inTable ? 'row' : 'column' }} className="form-item-composite-wrapper">
            {inTable
              ? inputFields
              : availableFieldNames.map((field, index) => getFormItem({ field, inputField: inputFields[index] }))}
          </div>
          {!inTable && currentLocationButtonVisible && (
            <IconButton onClick={this.onSetCurrentLocationClick}>
              <MyLocation />
            </IconButton>
          )}
          {dirty && <DirtyFieldSpinner />}
        </div>
      </>
    )
  }
}

const mapStateToProps = (state) => {
  const { session } = state
  const { loggedUser: user } = session ?? {}
  return { user }
}

export default connect(mapStateToProps)(CoordinateField)
