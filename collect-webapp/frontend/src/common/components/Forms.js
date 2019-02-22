import React, { Component } from 'react';
import { FormFeedback, FormGroup, Label, Input, Col } from 'reactstrap';
import L from 'utils/Labels'
import Strings from 'utils/Strings'

export class SimpleFormItem extends Component {

    extractErrorMessage(fieldId, errorFeedback, validationErrors) {
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

    render() {
        const {fieldId, label, fieldState, errorFeedback, validationErrors, check=false, row=true, labelColSpan=2, fieldColSpan=10} = this.props
        const errorMessage = this.extractErrorMessage(fieldId, errorFeedback, validationErrors)
        const formGroupColor = fieldState ? fieldState : errorMessage ? 'danger': null

        return (
            <FormGroup row={row} color={formGroupColor} check={check}>
                <Label check={check} for={fieldId} sm={labelColSpan}>{L.l(label)}:</Label>
                <Col sm={fieldColSpan}>
                    {check && 
                        <FormGroup check>
                            <Label check>
                                {this.props.children}
                            </Label>
                        </FormGroup>
                    }
                    {! check && this.props.children}
                    {errorMessage && <FormFeedback>{errorMessage}</FormFeedback>}
                </Col>
            </FormGroup>
        )
    }
}

export class FormItem extends Component {
    render() {
        const { label, error, touched, asyncValidating, labelColSpan=2, fieldColSpan=10 } = this.props
        
        return (
            <FormGroup row>
                <Label sm={labelColSpan}>{label}</Label>
                <Col sm={fieldColSpan} className={asyncValidating ? 'async-validating' : ''}>
                    {this.props.children}
                    {touched && error && <FormFeedback>{error}</FormFeedback>}
                </Col>
            </FormGroup>
        )
    }
}

export default class Forms {

    static handleValidationResponse(r) {
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

    static renderFormItemInputField({ input, label, type, contentEditable, labelColSpan=2, fieldColSpan=10, meta: { asyncValidating, touched, error } }) {
        return <FormItem label={label} asyncValidating={asyncValidating} touched={touched} error={error} 
                    labelColSpan={labelColSpan} fieldColSpan={fieldColSpan}>
                <Input readOnly={contentEditable === false} 
                    valid={touched && error ? false : null}
                    invalid={touched && error ? true: null} 
                    type={type}
                    {...input} />
            </FormItem>
    }

    static renderInputField({ input, label, type, contentEditable, meta: { asyncValidating, touched, error } }) {
        return <Input readOnly={contentEditable === false} valid={touched && error ? false : null} {...input} type={type} />
    }

    static renderFormItemSelect({ input, label, type, options, contentEditable, labelColSpan=2, fieldColSpan=10, meta: { asyncValidating, touched, error }}) {
        return <FormItem label={label} touched={touched} error={error}
                    labelColSpan={labelColSpan} fieldColSpan={fieldColSpan}>
                <Input readOnly={contentEditable === false} 
                    valid={touched && error ? false : null}
                    invalid={touched && error ? true: null} 
                    type="select" {...input}>{options}</Input>
            </FormItem>
    }

    static renderSelect({ input, label, type, options, contentEditable, meta: { asyncValidating, touched, error } }) {
        return <Input readOnly={contentEditable === false} valid={touched && error ? false : null} type="select" {...input}>{options}</Input>
    }
    /*
    static renderMaterialUIField = ({input, label, type, fullWidth, meta: { asyncValidating, touched, error }}) => 
        <FormControl error={touched && error} fullWidth={fullWidth} className={asyncValidating ? 'async-validating' : ''}>
                <InputLabel>{label}</InputLabel>
                <Input {...input} type={type}  />
                {touched && error && <FormHelperText>{error}</FormHelperText>}
        </FormControl>
    }
    */
    static normalizeInternalName = value => {
        if (value) {
            return Strings.replaceAll(value.toLowerCase(), '\\W', '_')
        } else {
            return value
        }
    }

    static getValidState(fieldId, validationErrors) {
        return validationErrors ? 
                validationErrors.find(e => e.field === fieldId) ? false 
                : null
            : null
    }

}