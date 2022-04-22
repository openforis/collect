import React, { useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import MuiAutocomplete, { createFilterOptions } from '@mui/material/Autocomplete'
import { Box, Popper, TextField } from '@mui/material'

import Arrays from 'utils/Arrays'
import LoadingSpinnerSmall from './LoadingSpinnerSmall'

const PopperCustom =
  ({ popUpWidth, popUpWidthContentBased }) =>
  (props) => {
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
    isItemEqualToValue,
    itemRenderFunction,
    itemLabelFunction,
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

  const renderOption = itemRenderFunction
    ? (renderProps, item) => <Box {...renderProps}>{itemRenderFunction({ renderProps, item })}</Box>
    : undefined

  const inputFieldWidthPx = inputFieldWidth instanceof Number ? `${String(inputFieldWidth)}px` : inputFieldWidth

  const renderInput = (params) => (
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
  )

  const isOptionEqualToValue = isItemEqualToValue
    ? (option, value) => isItemEqualToValue({ item: option, value })
    : null

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
      options={items}
      filterOptions={filterOptions}
      loading={loading}
      renderInput={renderInput}
      renderTags={tagsRenderFunction}
      renderOption={renderOption}
      className={className}
      disabled={disabled || readOnly}
      multiple={multiple}
      PopperComponent={PopperCustom({ popUpWidth, popUpWidthContentBased })}
      isOptionEqualToValue={isOptionEqualToValue}
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
  isItemEqualToValue: PropTypes.func,
  items: PropTypes.array,
  itemRenderFunction: PropTypes.func,
  itemLabelFunction: PropTypes.func,
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
  isItemEqualToValue: null,
  items: [],
  itemRenderFunction: null,
  itemLabelFunction: null,
  onInputChange: () => {},
  onDismiss: () => {},
  popUpWidth: null, // default to inputFieldWidth
  popUpWidthContentBased: false,
  readOnly: false,
  selectedItems: [],
  tagsRenderFunction: null,
}

export default Autocomplete
