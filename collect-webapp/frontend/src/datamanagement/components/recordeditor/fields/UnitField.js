import React from 'react'
import { FormControl, InputLabel, MenuItem, Select } from '@material-ui/core'
import L from 'utils/Labels'

const UnitField = (props) => {
  const { attributeDefinition, onChange, unitId } = props
  const { calculated, precisions } = attributeDefinition

  return (
    <FormControl>
      <InputLabel>{L.l('common.unit')}</InputLabel>
      <Select
        variant="outlined"
        value={String(unitId)}
        disabled={calculated}
        onChange={onChange}
        label={L.l('common.unit')}
      >
        {precisions.map((precision) => {
          const unit = attributeDefinition.survey.units.find((unit) => unit.id === precision.unitId)
          return (
            <MenuItem key={unit.id} value={String(unit.id)} title={unit.label}>
              {unit.abbreviation}
            </MenuItem>
          )
        })}
      </Select>
    </FormControl>
  )
}

export default UnitField
