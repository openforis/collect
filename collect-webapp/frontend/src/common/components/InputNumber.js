import React from 'react'
import PropTypes from 'prop-types'
import NumberFormat from 'react-number-format'
import { TextField } from '@material-ui/core'

const CustomInput = (inputProps) => {
  const { maxLength, onBlur, onChange, onFocus, onKeyDown, onMouseUp, readOnly, size, value, width } = inputProps

  const style = width ? { width: typeof width === 'string' ? width : `${width}px` } : {}

  return (
    <TextField
      disabled={readOnly}
      variant="outlined"
      onBlur={onBlur}
      onChange={onChange}
      onFocus={onFocus}
      onKeyDown={onKeyDown}
      onMouseUp={onMouseUp}
      value={value}
      style={style}
      inputProps={{
        maxLength,
        size,
      }}
    />
  )
}

const InputNumber = (props) => {
  const { decimalScale, maxLength, onChange: onChangeProps, readOnly, size, value, width } = props

  const onChange = (values) => {
    const { floatValue: valueChanged, value: inputValue } = values
    if (inputValue !== String(value)) {
      onChangeProps(valueChanged)
    }
  }

  return (
    <NumberFormat
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

InputNumber.defaultProps = {
  decimalScale: undefined,
  maxLength: undefined,
  readOnly: false,
  size: undefined,
  value: null,
  width: undefined,
}

export default InputNumber
