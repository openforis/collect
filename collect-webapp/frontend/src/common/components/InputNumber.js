import React from 'react'
import PropTypes from 'prop-types'
import { NumericFormat } from 'react-number-format'
import { TextField } from '@mui/material'

const CustomInput = (inputProps) => {
  const { maxLength, onBlur, onChange, onFocus, onKeyDown, onMouseUp, readOnly, size, value, width } = inputProps

  const style = width ? { width: typeof width === 'string' ? width : `${width}px` } : {}

  return (
    <TextField
      autoComplete="off"
      disabled={readOnly}
      variant="outlined"
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onKeyDown={onKeyDown}
      onMouseUp={onMouseUp}
      value={value}
      style={style}
      slotProps={{
        htmlInput: {
          maxLength,
          size,
        },
      }}
    />
  )
}

const InputNumber = (props) => {
  const { decimalScale, maxLength, onChange: onChangeProps, readOnly = false, size, value = null, width } = props

  const onChange = (values) => {
    const { floatValue: valueChanged, value: inputValue } = values
    if (inputValue !== String(value)) {
      onChangeProps(valueChanged)
    }
  }

  return (
    <NumericFormat
      decimalScale={decimalScale}
      maxLength={maxLength}
      onValueChange={onChange}
      readOnly={readOnly}
      size={size}
      value={value}
      width={width}
      customInput={CustomInput}
    />
  )
}

InputNumber.propTypes = {
  decimalScale: PropTypes.number,
  maxLength: PropTypes.number,
  onChange: PropTypes.func.isRequired,
  readOnly: PropTypes.bool,
  size: PropTypes.number,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  width: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
}

export default InputNumber
