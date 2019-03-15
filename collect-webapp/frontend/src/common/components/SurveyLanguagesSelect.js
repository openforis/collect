import React from 'react'

import MenuItem from '@material-ui/core/MenuItem'
import Select from '@material-ui/core/Select'

import L from '../../utils/Labels'

const SurveyLanguagesSelect = ({ survey, value, onChange }) =>
    <Select
        value={value}
        onChange={e => onChange(e.target.value)}>
        {survey.languages.map(l =>
            <MenuItem key={l} value={l}>{L.l('languages.' + l) + ' (' + l + ')'}</MenuItem>
        )}
    </Select>

export default SurveyLanguagesSelect