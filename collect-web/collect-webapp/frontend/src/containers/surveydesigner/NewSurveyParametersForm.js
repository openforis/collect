import React, { Component } from 'react';
import { Field, SubmissionError, reduxForm } from 'redux-form'
import { Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
	DropdownToggle, DropdownMenu, DropdownItem, Form, FormFeedback, FormGroup, Label, Input, Row, Col, UncontrolledDropdown } from 'reactstrap';
import { connect } from 'react-redux';
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'

const templateTypes = ['BLANK', 'BIOPHYSICAL', 'COLLECT_EARTH', 'COLLECT_EARTH_IPCC']

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))

const submit = values => {
    return sleep(1000).then(() => {
        
    })
}

const asyncValidate = (values /*, dispatch */) => {
    ServiceFactory.surveyService.validateSurveyCreation(values.name, values.templateType, values.defaultLanguageCode, values.userGroupId).then(response => {
        console.log(response)
    })
    /*
    return sleep(20000).then(() => {
      // simulate server latency
      if (['john', 'paul', 'george', 'ringo'].includes(values.name)) {
        throw { name: 'That username is taken' }
      }
    })
    */
  }

class NewSurveyParametersForm extends Component {
    constructor(props) {
        super(props)
    }

    render() {
        const { userGroups, error, handleSubmit, pristine, reset, submitting } = this.props

        const templateTypeOptions = templateTypes.map(type => <option key={type} value={type}>{L.l('survey.templateType.' + type)}</option>)
        const userGroupOptions = userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>)
        const languageOptions = L.keys('languages').map(l => <option key={l} value={l}>{L.l('languages.' + l)}</option>)

        const renderInputField = ({ input, label, type, meta: { asyncValidating, touched, error } }) =>
            <FormGroup row>
                <Label sm={2}>{label}</Label>
                <Col sm={10} className={asyncValidating ? 'async-validating' : ''}>
                    <Input valid={error ? false: ''} {...input} type={type} />
                    {touched && error && <FormFeedback>{error}</FormFeedback>}
                </Col>
            </FormGroup>
        
        const renderSelect = ({ input, label, type, options, meta: { asyncValidating, touched, error } }) =>
            <FormGroup row>
                <Label sm={2}>{label}</Label>
                <Col sm={10} className={asyncValidating ? 'async-validating' : ''}>
                    <Input type="select" {...input}>{options}</Input>
                    {touched && error && <span>{error}</span>}
                </Col>
            </FormGroup>


        return (
            <Form onSubmit={handleSubmit(submit)}>
                <Field
                    name="name"
                    type="text"
                    component={renderInputField}
                    label={L.l('survey.name')}
                />
                <Field
                    name="templateType"
                    component={renderSelect}
                    label={L.l('survey.templateType')}
                    options={templateTypeOptions}
                />
                <Field
                    name="defaultLanguageCode"
                    component={renderSelect}
                    label={L.l('survey.defaultLanguage')}
                    options={languageOptions}
                />
                <Field
                    name="userGroupId"
                    multi
                    component={renderSelect}
                    label={L.l('survey.userGroup')}
                    options={userGroupOptions}
                />
                {error &&
                    <strong>
                        {error}
                    </strong>}
                <div>
                    <button type="submit" disabled={submitting}>{L.l('general.new')}</button>
                </div>
            </Form>
        )
    }
}

const mapStateToProps = state => {
    const {items: userGroups} = state.userGroups
    return {
        userGroups
    }
}

NewSurveyParametersForm = connect(mapStateToProps)(NewSurveyParametersForm)

export default reduxForm({ 
    form: 'newSurveyParametersForm',
    asyncValidate,
    asyncBlurFields: ['name'],
    initialValues: {
        templateType: 'BLANK',
        defaultLanguageCode: 'en',
        userGroupId: -1
    }
})(NewSurveyParametersForm)