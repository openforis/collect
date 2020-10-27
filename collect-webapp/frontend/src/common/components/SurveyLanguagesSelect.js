import React from 'react'

import MenuItem from '@material-ui/core/MenuItem'
import Select from '@material-ui/core/Select'

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
