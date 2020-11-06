import React, { useEffect, useState } from 'react'
import Autocomplete from '@material-ui/lab/Autocomplete'
import { TextField } from '@material-ui/core'

import LoadingSpinnerSmall from './LoadingSpinnerSmall'

const AutocompleteAsync = (props) => {
  const {
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
    items: [],
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

  // on dialog open, trigger loading
  useEffect(() => {
    const stateUpdated = { loading: open }
    if (!open) {
      stateUpdated.items = []
    }
    setState(stateUpdated)
  }, [open])

  // on input value change re-fetch items
  useEffect(() => {
    if (open) {
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

  return (
    <Autocomplete
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
      filterOptions={() => items}
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

AutocompleteAsync.defaultProps = {
  inputValue: '',
  selectedItem: null,
  onInputChange: () => {},
  onDismiss: () => {},
  optionRenderFunction: null,
}

export default AutocompleteAsync
