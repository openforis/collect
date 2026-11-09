import './CodeFieldText.css'

import React, { useState } from 'react'
import { IconButton, TextField as MuiTextField } from '@mui/material'
import ListIcon from '@mui/icons-material/List'

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

  const valueLabelFunction = ({ item, value }) => {
    const { qualifier } = value

    return `${itemLabelFunction(item)}${qualifier ? `: ${qualifier}` : ''}`
  }

  const valuesString = selectedItems
    .map((item, index) => {
      const value = values[index]
      return valueLabelFunction({ item, value })
    })
    .join('; ')

  const [dialogOpen, setDialogOpen] = useState(false)

  return <>
    <div className="code-field-text-wrapper">
      <MuiTextField
        variant="outlined"
        disabled
        value={valuesString}
        title={selectedItems.map(itemLabelFunction).join('; ')}
        onClick={() => setDialogOpen(true)}
      />
      <IconButton onClick={() => setDialogOpen(true)} size="large">
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
  </>;
}

export default CodeFieldText
