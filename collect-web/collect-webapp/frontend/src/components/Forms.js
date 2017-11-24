import React, { Component } from 'react';
import { FormFeedback, FormGroup, Label, Input, Col } from 'reactstrap';
import L from 'utils/Labels'

class FormItem extends Component {
    render() {
        const { label, error, touched, asyncValidating } = this.props
        
        return (
            <FormGroup row>
                <Label sm={2}>{label}</Label>
                <Col sm={10} className={asyncValidating ? 'async-validating' : ''}>
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
            errors.forEach(error => {
                result[error.field] = L.l(error.code, error.arguments)
            })
            result._error = L.l('validation.errorsInTheForm')
            throw result
        }
    }

    static renderInputField({ input, label, type, meta: { asyncValidating, touched, error } }) {
        return <FormItem label={label} asyncValidating={asyncValidating} touched={touched} error={error}>
                <Input valid={error ? false : ''} {...input} type={type} />
            </FormItem>
    }

    static renderSelect({ input, label, type, options, meta: { asyncValidating, touched, error } }) {
        return <FormItem label={label} touched={touched} error={error}>
                <Input valid={error ? false : ''} type="select" {...input}>{options}</Input>
            </FormItem>
    }
}