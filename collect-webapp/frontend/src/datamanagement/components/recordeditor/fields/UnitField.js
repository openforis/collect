import React from 'react'
import { MenuItem, Select } from '@material-ui/core'
import L from 'utils/Labels'

const UnitField = (props) => {
  const { attributeDefinition, onChange, unitId } = props
  const { calculated, precisions } = attributeDefinition

  const selectedUnitId = unitId ? String(unitId) : ''

  return (
    <Select variant="outlined" value={selectedUnitId} disabled={calculated} onChange={onChange}>
      <MenuItem key="" value="">
        <em>{L.l('common.selectOne')}</em>
      </MenuItem>
      {precisions.map((precision) => {
        const unit = attributeDefinition.survey.units.find((unit) => unit.id === precision.unitId)
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
