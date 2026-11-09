import React from 'react'

import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'

import Languages from 'utils/Languages'

const SurveyLanguagesSelect = ({ survey, value, onChange }) => (
  <Select value={value} onChange={(e) => onChange(e.target.value)}>
    {survey.languages.map((langCode) => (
      <MenuItem key={langCode} value={langCode}>
        {Languages.label(langCode) + ' (' + langCode + ')'}
      </MenuItem>
    ))}
  </Select>
)

export default SurveyLanguagesSelect
