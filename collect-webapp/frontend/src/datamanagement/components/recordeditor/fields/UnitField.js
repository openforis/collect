import React from 'react'
import { MenuItem, Select } from '@mui/material'
import L from 'utils/Labels'

const getUnit = ({ attributeDefinition, precision }) =>
  attributeDefinition.survey.units.find((unit) => unit.id === precision.unitId)

const UnitField = (props) => {
  const { attributeDefinition, onChange, readOnly, unitId } = props
  const { precisions } = attributeDefinition

  const selectedUnitId = unitId ? String(unitId) : ''

  if (precisions.length === 1) {
    const unit = getUnit({ attributeDefinition, precision: precisions[0] })
    return <label title={unit.label}>{unit.abbreviation}</label>
  }

  return (
    <Select variant="outlined" value={selectedUnitId} disabled={readOnly} onChange={onChange}>
      <MenuItem key="" value="">
        <em>{L.l('common.selectOne')}</em>
      </MenuItem>
      {precisions.map((precision) => {
        const unit = getUnit({ attributeDefinition, precision })
        return (
          <MenuItem key={unit.id} value={String(unit.id)} title={unit.label}>
            {unit.abbreviation}
          </MenuItem>
        )
      })}
    </Select>
  )
}

export default UnitField
