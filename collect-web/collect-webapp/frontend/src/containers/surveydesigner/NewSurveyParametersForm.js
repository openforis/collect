import React, { Component } from 'react';
import { Field, SubmissionError, reduxForm } from 'redux-form'
import { Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
	DropdownToggle, DropdownMenu, DropdownItem, Input, Row, Col, UncontrolledDropdown } from 'reactstrap';
import { connect } from 'react-redux';
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'

const templateTypes = ['BLANK', 'BIOPHYSICAL', 'COLLECT_EARTH', 'COLLECT_EARTH_IPCC']

const submit = values => {
    const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))
    return sleep(1000).then(() => {
        
    })
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

        const renderInputField = ({ input, label, type, meta: { touched, error } }) =>
            <div>
                <label>{label}</label>
                <div>
                    <input {...input} placeholder={label} type={type} />
                    {touched && error && <span>{error}</span>}
                </div>
            </div>
        
        const renderSelect = ({ input, label, type, options, meta: { touched, error } }) =>
            <div>
                <label>{label}</label>
                <div>
                    <Input type="select" {...input} placeholder={label}>{options}</Input>
                    {touched && error && <span>{error}</span>}
                </div>
            </div>


        return (
            <form onSubmit={handleSubmit(submit)}>
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
            </form>
        )
    }
}

const mapStateToProps = state => {
    const {items: userGroups} = state.userGroups
    return {
        userGroups
    }
}

export default 
    connect(mapStateToProps)(
        reduxForm({ 
            form: 'newSurveyParametersForm',
            initialValues: {
                templateType: 'BLANK',
                defaultLanguageCode: 'en',
                userGroupId: -1
            }
        })(NewSurveyParametersForm)
)