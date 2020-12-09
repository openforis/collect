import './CodeFieldDialog.css'

import React, { useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@material-ui/core'

import L from 'utils/Labels'
import CodeFieldRadio from './CodeFieldRadio'
import { useEffect } from 'react'
import CodeFieldAutocomplete from './CodeFieldAutocomplete'

const CodeFieldDialog = (props) => {
  const {
    ancestorCodes,
    asynchronous,
    attributeDefinition,
    fieldDef,
    itemLabelFunction,
    items: itemsProps,
    onChange,
    onChangeQualifier,
    onClose,
    parentEntity,
    selectedItems,
    values,
  } = props

  const { calculated, multiple } = attributeDefinition
  const { record } = parentEntity
  const readOnly = record.readOnly || calculated

  const [items, setItems] = useState([])

  useEffect(() => {
    setItems(asynchronous ? selectedItems : itemsProps)
  }, [asynchronous, selectedItems, itemsProps])

  return (
    <Dialog open>
      <DialogTitle>{attributeDefinition.labelOrName}</DialogTitle>
      <DialogContent style={{ height: '400px', width: '600px' }}>
        {asynchronous && !readOnly && (
          <div className="code-field-dialog-autocomplete-wrapper">
            <label>{L.l('common.search')}:</label>
            <CodeFieldAutocomplete
              parentEntity={parentEntity}
              fieldDef={fieldDef}
              asynchronous
              ancestorCodes={ancestorCodes}
              itemLabelFunction={itemLabelFunction}
              width="100%"
              onSelect={(item) => onChange({ item, selected: true })}
            />
          </div>
        )}
        <CodeFieldRadio
          parentEntity={parentEntity}
          attributeDefinition={attributeDefinition}
          values={values}
          items={items}
          onChange={onChange}
          onChangeQualifier={onChangeQualifier}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{L.l('common.close')}</Button>
      </DialogActions>
    </Dialog>
  )
}

export default CodeFieldDialog
