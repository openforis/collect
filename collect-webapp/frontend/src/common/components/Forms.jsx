import React from 'react'
import { Button, FormFeedback, FormGroup, Label, Input, Col } from 'reactstrap'
import L from 'utils/Labels'
import Strings from 'utils/Strings'
import Objects from '../../utils/Objects'
import Arrays from 'utils/Arrays'

export const SimpleFormItem = (props) => {
  const extractErrorMessage = (fieldId, errorFeedback, validationErrors) => {
    let errorMessage = null
    let errorMessageArgs = null
    if (errorFeedback) {
      errorMessage = errorFeedback
    } else if (validationErrors) {
      const error = validationErrors.find((error) => error.field === fieldId)
      if (error) {
        errorMessage = error.code
        errorMessageArgs = error.arguments
      }
    }
    if (Objects.isEmpty(errorMessage)) return null
    const errorMessages = Arrays.toArray(errorMessage)
    return errorMessages
      .map((messageKeyOrObject) => {
        const messageKey = typeof messageKeyOrObject === 'object' ? messageKeyOrObject.messageKey : messageKeyOrObject
        const messageArgs = typeof messageKeyOrObject === 'object' ? messageKeyOrObject.messageArgs : errorMessageArgs
        return L.l(messageKey, messageArgs)
      })
      .join(', ')
  }

  const {
    fieldId,
    label,
    fieldState,
    errorFeedback,
    validationErrors,
    check = false,
    row = true,
    labelColSpan = 2,
    fieldColSpan = 10,
  } = props
  const errorMessage = extractErrorMessage(fieldId, errorFeedback, validationErrors)
  const formGroupColor = fieldState ? fieldState : errorMessage ? 'danger' : null

  return (
    <FormGroup row={row} color={formGroupColor} check={check}>
      <Label check={check} for={fieldId} sm={labelColSpan}>
        {L.l(label)}:
      </Label>
      <Col sm={fieldColSpan}>
        {check && (
          <FormGroup check>
            <Label check>{props.children}</Label>
          </FormGroup>
        )}
        {!check && props.children}
        {errorMessage && <FormFeedback>{errorMessage}</FormFeedback>}
      </Col>
    </FormGroup>
  )
}

export const FormItem = (props) => {
  const { label, error, touched, asyncValidating, labelColSpan = 2, fieldColSpan = 10 } = props

  return (
    <FormGroup row>
      <Label sm={labelColSpan}>{label}</Label>
      <Col sm={fieldColSpan} className={asyncValidating ? 'async-validating' : ''}>
        {props.children}
        {touched && error && <FormFeedback>{error}</FormFeedback>}
      </Col>
    </FormGroup>
  )
}

export const InputFormItem = (props) => {
  const {
    name,
    type,
    label,
    readOnly = false,
    disabled = false,
    asyncValidating,
    touched,
    errors,
    values,
    handleChange,
    handleBlur,
    labelColSpan,
    fieldColSpan,
  } = props
  const value = values[name] || ''
  const error = errors[name]
  const fieldTouched = touched[name]
  return (
    <FormItem
      label={label}
      asyncValidating={asyncValidating}
      touched={fieldTouched}
      error={error}
      labelColSpan={labelColSpan}
      fieldColSpan={fieldColSpan}
    >
      <Input
        name={name}
        type={type}
        value={value}
        onBlur={handleBlur}
        onChange={handleChange}
        readOnly={readOnly}
        disabled={disabled}
        valid={fieldTouched && error ? false : null}
        invalid={fieldTouched && error ? true : null}
      >
        {props.children}
      </Input>
    </FormItem>
  )
}

export const TextFormItem = (props) => <InputFormItem type="text" {...props} />
export const SelectFormItem = (props) => <InputFormItem type="select" {...props} />

export const SubmitButton = (props) => {
  const { submitting, errors, children } = props

  return (
    <Button color="primary" type="submit" disabled={submitting || Objects.isNotEmpty(errors)}>
      {children}
    </Button>
  )
}

export const normalizeInternalName = (value) => (value ? Strings.replaceAll(value.toLowerCase(), '\\W', '_') : value)

export const getValidState = (fieldId, validationErrors) =>
  validationErrors && validationErrors.find((e) => e.field === fieldId) ? false : null

const _extractErrorsFromValidationResponse = (validationResponse) => {
  if (validationResponse.statusError) {
    const result = {}
    const errors = validationResponse.objects.errors
    if (errors) {
      errors.forEach((error) => {
        result[error.field] = L.l(error.code, error.arguments)
      })
    }
    let errorMessage = L.l('validation.errorsInTheForm')
    if (validationResponse.errorMessage) {
      errorMessage += ': ' + validationResponse.errorMessage
    }
    result._error = errorMessage
    return result
  }
  return null
}

export const handleValidationResponse = (validationResponse) => {
  const errorResult = _extractErrorsFromValidationResponse(validationResponse)
  if (errorResult) {
    throw errorResult
  }
}

export const asyncValidate =
  (validateFn) =>
  (...params) =>
    new Promise((resolve, reject) => {
      validateFn
        .apply(null, params)
        .then((validationResponse) => {
          try {
            const errorResult = _extractErrorsFromValidationResponse(validationResponse)
            resolve(errorResult)
          } catch (error) {
            reject(error)
          }
        })
        .catch(reject)
    })
