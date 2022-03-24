import React, { useState } from 'react'
import { debounce } from 'throttle-debounce'

import { QuickSearchField } from '../QuickSearchField'

export const QuickSearchHeader = (props) => {
  const { onChange: onChangeProp, headerName } = props
  const [value, setValue] = useState('')

  const onValueChange = (val) => {
    setValue(val)
    debounce(500, false, async () => onChangeProp(val))()
  }

  return (
    <div className="quick-search-header">
      <span>{headerName}</span>
      <QuickSearchField
        value={value}
        onChange={(event) => onValueChange(event.target.value)}
        clearSearch={() => onValueChange('')}
      />
    </div>
  )
}
