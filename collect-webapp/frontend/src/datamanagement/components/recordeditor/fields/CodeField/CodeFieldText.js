import './CodeFieldText.css'

import React, { useState } from 'react'
import { IconButton, TextField as MuiTextField } from '@material-ui/core'
import ListIcon from '@material-ui/icons/List'

import CodeFieldDialog from './CodeFieldDialog'

const CodeFieldText = (props) => {
  const {
    ancestorCodes,
    asynchronous,
    attributeDefinition,
    fieldDef,
    itemLabelFunction,
    items,
    onChange,
    onChangeQualifier,
    readOnly,
    parentEntity,
    selectedItems,
    values,
  } = props

  const { showCode } = attributeDefinition

  const valueLabelFunction = ({ item, value }) => {
    const { code } = item
    const { qualifier } = value
    const label = itemLabelFunction(item)

    return `${showCode && code !== label ? `${code} - ` : ''}${label}${qualifier ? `: ${qualifier}` : ''}`
  }

  const valuesString = selectedItems
    .map((item, index) => {
      const value = values[index]
      return valueLabelFunction({ item, value })
    })
    .join('; ')

  const [dialogOpen, setDialogOpen] = useState(false)

  return (
    <>
      <div className="code-field-text-wrapper">
        <MuiTextField
          variant="outlined"
          disabled
          value={valuesString}
          title={selectedItems.map(itemLabelFunction).join('; ')}
          onClick={() => setDialogOpen(true)}
        />
        <IconButton onClick={() => setDialogOpen(true)}>
          <ListIcon />
        </IconButton>
      </div>
      {dialogOpen && (
        <CodeFieldDialog
          asynchronous={asynchronous}
          parentEntity={parentEntity}
          ancestorCodes={ancestorCodes}
          attributeDefinition={attributeDefinition}
          fieldDef={fieldDef}
          itemLabelFunction={itemLabelFunction}
          items={items}
          onChange={onChange}
          onChangeQualifier={onChangeQualifier}
          onClose={() => setDialogOpen(false)}
          readOnly={readOnly}
          selectedItems={selectedItems}
          values={values}
        />
      )}
    </>
  )
}

export default CodeFieldText
