import React from 'react';
import { Button, FormFeedback, FormGroup, Label, Input, Col } from 'reactstrap';
import L from 'utils/Labels'
import Strings from 'utils/Strings'
import Objects from '../../utils/Objects';

export const SimpleFormItem = props => {

    const extractErrorMessage = (fieldId, errorFeedback, validationErrors) => {
        let errorMessage = null
        let errorMessageArgs = null
        if (errorFeedback) {
            errorMessage = errorFeedback
        } else if (validationErrors) {
            const error = validationErrors.find(error => error.field === fieldId)
            if (error) {
                errorMessage = error.code
                errorMessageArgs = error.arguments
            }
        }
        errorMessage = L.l(errorMessage, errorMessageArgs)
        return errorMessage
    }


    const { fieldId, label, fieldState, errorFeedback, validationErrors, check = false, row = true, labelColSpan = 2, fieldColSpan = 10 } = props
    const errorMessage = extractErrorMessage(fieldId, errorFeedback, validationErrors)
    const formGroupColor = fieldState ? fieldState : errorMessage ? 'danger' : null

    return (
        <FormGroup row={row} color={formGroupColor} check={check}>
            <Label check={check} for={fieldId} sm={labelColSpan}>{L.l(label)}:</Label>
            <Col sm={fieldColSpan}>
                {check &&
                    <FormGroup check>
                        <Label check>
                            {props.children}
                        </Label>
                    </FormGroup>
                }
                {!check && props.children}
                {errorMessage && <FormFeedback>{errorMessage}</FormFeedback>}
            </Col>
        </FormGroup>
    )
}


export const FormItem = props => {
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


export const SelectFormItem = props => {
    const {
        name, label, touched, errors,
        labelColSpan, fieldColSpan,
        readOnly = false, options, values,
        handleChange, handleBlur
    } = props
    const value = values[name] || ''
    const error = errors[name]
    const fieldTouched = touched[name]
    return (
        <FormItem label={label} touched={fieldTouched} error={error}
            labelColSpan={labelColSpan} fieldColSpan={fieldColSpan} >
            <Input
                name={name}
                readOnly={readOnly}
                valid={fieldTouched && error ? false : null}
                invalid={fieldTouched && error ? true : null}
                type="select"
                onBlur={handleBlur}
                onChange={handleChange}
                value={value}>{options}</Input>
        </FormItem >
    )
}

export const TextFormItem = props => {
    const {
        name, type = "text", label, asyncValidating, touched, errors,
        labelColSpan, fieldColSpan,
        readOnly = false,
        values,
        handleChange, handleBlur
    } = props
    const value = values[name] || ''
    const error = errors[name]
    const fieldTouched = touched[name]
    return (
        <FormItem label={label} asyncValidating={asyncValidating} touched={fieldTouched} error={error}
            labelColSpan={labelColSpan} fieldColSpan={fieldColSpan}>
            <Input
                name={name}
                readOnly={readOnly}
                valid={fieldTouched && error ? false : null}
                invalid={fieldTouched && error ? true : null}
                type={type}
                onBlur={handleBlur}
                onChange={handleChange}
                value={value} />
        </FormItem>
    )
}

export const SubmitButton = props => {
    const { submitting, errors, children } = props

    return <Button color="primary" type="submit" disabled={submitting || Objects.isNotEmpty(errors)}>{children}</Button>
}

export const normalizeInternalName = value =>
    value
        ? Strings.replaceAll(value.toLowerCase(), '\\W', '_')
        : value

export const getValidState = (fieldId, validationErrors) =>
    validationErrors && validationErrors.find(e => e.field === fieldId)
        ? false
        : null

export const handleValidationResponse = r => {
    if (r.statusError) {
        let result = {}
        const errors = r.objects.errors
        if (errors) {
            errors.forEach(error => {
                result[error.field] = L.l(error.code, error.arguments)
            })
        }
        let errorMessage = L.l('validation.errorsInTheForm')
        if (r.errorMessage) {
            errorMessage += ': ' + r.errorMessage
        }
        result._error = errorMessage
        throw result
    }
}

export const asyncValidate = validateFn => (...params) =>
    new Promise((resolve, reject) => {
        validateFn.apply(null, params)
            .then(r => {
                try {
                    handleValidationResponse(r)
                    resolve()
                } catch (error) {
                    reject(error)
                }
            })
            .catch(reject)
    })