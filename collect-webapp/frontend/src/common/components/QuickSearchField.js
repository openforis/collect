import * as React from 'react'
import PropTypes from 'prop-types'
import { Clear, Search } from '@material-ui/icons'

import IconButton from '@material-ui/core/IconButton'
import TextField from '@material-ui/core/TextField'

export const QuickSearchField = (props) => {
  const { clearSearch, onChange, value } = props

  return (
    <TextField
      variant="standard"
      value={value}
      onChange={onChange}
      onClick={(e) => {
        e.preventDefault()
        e.stopPropagation()
      }}
      placeholder="Search…"
      InputProps={{
        startAdornment: <Search fontSize="small" />,
        endAdornment: (
          <IconButton
            title="Clear"
            aria-label="Clear"
            size="small"
            style={{ visibility: value ? 'visible' : 'hidden' }}
            onClick={clearSearch}
          >
            <Clear fontSize="small" />
          </IconButton>
        ),
      }}
    />
  )
}

QuickSearchField.propTypes = {
  clearSearch: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  value: PropTypes.string.isRequired,
}
