import React, { useEffect, useState } from 'react'
import { debounce } from 'throttle-debounce'

import { QuickSearchField } from '../QuickSearchField'

export const QuickSearchHeader = (props) => {
  const { field, filterModel, headerName, onChange: onChangeProp } = props

  const [value, setValue] = useState('')

  useEffect(() => {
    if (filterModel?.items?.[0]?.columnField !== field) {
      setValue('') // only one search field at a time is supported
    }
  }, [filterModel])

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
