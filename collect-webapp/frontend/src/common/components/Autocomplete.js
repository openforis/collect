import React, { useEffect, useState } from 'react'
import PropTypes from 'prop-types'
import MuiAutocomplete, { createFilterOptions } from '@material-ui/lab/Autocomplete'
import { TextField } from '@material-ui/core'

import LoadingSpinnerSmall from './LoadingSpinnerSmall'

const Autocomplete = (props) => {
  const {
    asynchronous,
    items: itemsProps,
    inputValue: initialInputValue,
    inputFieldWidth,
    selectedItem,
    fetchFunction,
    optionRenderFunction,
    optionLabelFunction,
    optionSelectedFunction,
    onInputChange: onInputChangeProps,
    onSelect,
    onDismiss,
  } = props

  const [state, setStateInternal] = useState({
    open: false,
    loading: false,
    items: itemsProps || [],
    inputValue: initialInputValue,
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

  const onClose = (_, reason) => {
    setState({ open: false })
    if (['escape', 'blur'].includes(reason)) {
      onDismiss()
    }
  }

  const filterOptions = asynchronous ? () => items : createFilterOptions()

  return (
    <MuiAutocomplete
      open={open}
      openOnFocus={false}
      onOpen={onOpen}
      onClose={onClose}
      value={selectedItem}
      inputValue={inputValue}
      onChange={(_, item) => onSelect(item)}
      onInputChange={onInputChange}
      getOptionLabel={optionLabelFunction}
      getOptionSelected={optionSelectedFunction}
      options={items}
      filterOptions={filterOptions}
      loading={loading}
      renderInput={(params) => (
        <TextField
          {...params}
          fullWidth={false}
          style={{ width: `${inputFieldWidth}px` }}
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
      renderOption={optionRenderFunction}
    />
  )
}

Autocomplete.propTypes = {
  asynchronous: PropTypes.bool,
  items: PropTypes.array,
  inputValue: PropTypes.string,
  inputFieldWidth: PropTypes.number,
  selectedItem: PropTypes.object,
  fetchFunction: PropTypes.func,
  optionRenderFunction: PropTypes.func,
  optionLabelFunction: PropTypes.func,
  optionSelectedFunction: PropTypes.func,
  onInputChange: PropTypes.func,
  onSelect: PropTypes.func.isRequired,
  onDismiss: PropTypes.func,
}

Autocomplete.defaultProps = {
  asynchronous: false,
  items: [],
  inputValue: '',
  inputFieldWidth: 300,
  selectedItem: null,
  onInputChange: () => {},
  onDismiss: () => {},
  fetchFunction: null,
  optionRenderFunction: null,
  optionLabelFunction: null,
  optionSelectedFunction: null,
}

export default Autocomplete
