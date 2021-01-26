import React, { useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import MuiAutocomplete, { createFilterOptions } from '@material-ui/lab/Autocomplete'
import { Popper, TextField } from '@material-ui/core'

import Arrays from 'utils/Arrays'
import LoadingSpinnerSmall from './LoadingSpinnerSmall'

const PopperCustom = ({ popUpWidth, popUpWidthContentBased }) => (props) => {
  const { style: styleProps = {} } = props
  const width = popUpWidthContentBased ? null : popUpWidth || styleProps?.width
  const style = { ...styleProps, minWidth: '100px', width: width ? `${width}px` : 'auto' }
  return <Popper {...props} style={style} placement="bottom-start" />
}

const Autocomplete = (props) => {
  const {
    asynchronous,
    multiple,
    className,
    disabled,
    items: itemsProps,
    inputValue: initialInputValue,
    inputFieldWidth,
    popUpWidth,
    popUpWidthContentBased,
    selectedItems,
    fetchFunction,
    itemRenderFunction,
    itemLabelFunction,
    itemSelectedFunction,
    onInputChange: onInputChangeProps,
    readOnly,
    tagsRenderFunction,
    onSelect,
    onDismiss,
  } = props

  const [state, setStateInternal] = useState({
    open: false,
    loading: false,
    items: itemsProps || [],
    inputValue: initialInputValue || selectedItems.length > 0 ? itemLabelFunction(selectedItems[0]) : '',
    fetchDebounced: null,
  })
  const setState = (stateUpdated) => setStateInternal({ ...state, ...stateUpdated })

  const { open, loading, items, inputValue, fetchDebounced } = state

  // fetch items on "open" and "inputValue" change
  useEffect(() => {
    if (!loading) {
      return undefined
    }

    let active = true // prevents rendering of an unmounted component

    if (fetchDebounced) {
      fetchDebounced.cancel()
    }
    const fetchDebouncedNew = fetchFunction({
      searchString: inputValue,
      onComplete: (itemsFetched) => {
        if (active) {
          setState({ items: itemsFetched, loading: false })
        }
      },
    })
    setState({ fetchDebounced: fetchDebouncedNew })

    fetchDebouncedNew()

    return () => {
      active = false
    }
  }, [loading, inputValue])

  // set input initial value on "initialInputValue" change (if dialog not open)
  useEffect(() => {
    if (!open) {
      setState({ inputValue: initialInputValue })
    }
  }, [initialInputValue])

  // on inputProps change, close dialog and update state items
  useEffect(() => {
    setState({ open: false, items: itemsProps || [] })
  }, [itemsProps])

  // on dialog open, trigger loading (if asyncrhonous)
  useEffect(() => {
    if (asynchronous) {
      const stateUpdated = { loading: open }
      if (!open) {
        stateUpdated.items = []
      }
      setState(stateUpdated)
    }
  }, [open])

  // on input value change re-fetch items
  useEffect(() => {
    if (asynchronous && open) {
      setState({ items: [], loading: true })
    }
  }, [inputValue])

  // on input change, notify external component
  const onInputChange = (_event, value, reason) => {
    if (reason === 'input') {
      onInputChangeProps(value)
    }
    setState({ inputValue: value })
  }

  const onOpen = () => setState({ open: true })

  const onClose = async (_, reason) => {
    await setState({ open: false })
    if (['escape', 'blur'].includes(reason)) {
      onDismiss()
    }
  }

  const filterOptions = asynchronous ? () => items : createFilterOptions()

  const inputFieldWidthPx = inputFieldWidth instanceof Number ? `${String(inputFieldWidth)}px` : inputFieldWidth

  return (
    <MuiAutocomplete
      open={open}
      size="small"
      openOnFocus={false}
      onOpen={onOpen}
      onClose={onClose}
      value={multiple ? selectedItems : Arrays.head(selectedItems)}
      inputValue={inputValue}
      onChange={(_, selection) => onSelect(selection, inputValue)}
      onInputChange={onInputChange}
      getOptionLabel={itemLabelFunction}
      getOptionSelected={itemSelectedFunction}
      options={items}
      filterOptions={filterOptions}
      loading={loading}
      renderInput={(params) => (
        <TextField
          {...params}
          fullWidth={inputFieldWidth === '100%'}
          style={{ width: inputFieldWidthPx }}
          variant="outlined"
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading && <LoadingSpinnerSmall />}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
      renderTags={tagsRenderFunction}
      renderOption={itemRenderFunction}
      className={className}
      disabled={disabled || readOnly}
      multiple={multiple}
      PopperComponent={PopperCustom({ popUpWidth, popUpWidthContentBased })}
    />
  )
}

Autocomplete.propTypes = {
  asynchronous: PropTypes.bool,
  multiple: PropTypes.bool,
  fetchFunction: PropTypes.func,
  disabled: PropTypes.bool,
  className: PropTypes.string,
  inputValue: PropTypes.string, // text shown in input field
  inputFieldWidth: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  items: PropTypes.array,
  itemRenderFunction: PropTypes.func,
  itemLabelFunction: PropTypes.func,
  itemSelectedFunction: PropTypes.func,
  readOnly: PropTypes.bool,
  selectedItems: PropTypes.array,
  tagsRenderFunction: PropTypes.func,
  onSelect: PropTypes.func.isRequired,
  onInputChange: PropTypes.func,
  onDismiss: PropTypes.func,
  popUpWidth: PropTypes.number,
  popUpWidthLimited: PropTypes.bool,
  readOnly: PropTypes.bool,
  selectedItems: PropTypes.array,
  tagsRenderFunction: PropTypes.func,
}

Autocomplete.defaultProps = {
  asynchronous: false,
  multiple: false,
  fetchFunction: null,
  disabled: false,
  className: null,
  inputValue: null,
  inputFieldWidth: 300,
  items: [],
  itemRenderFunction: null,
  itemLabelFunction: null,
  itemSelectedFunction: null,
  onInputChange: () => {},
  onDismiss: () => {},
  popUpWidth: null, // default to inputFieldWidth
  popUpWidthContentBased: false,
  readOnly: false,
  selectedItems: [],
  tagsRenderFunction: null,
}

export default Autocomplete
