import './NewSurveyParametersForm.css'

import React from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { withFormik } from 'formik'
import { Form, Row, Col, FormGroup, Label } from 'reactstrap'

import User from 'model/User'

import {
  TextFormItem,
  SelectFormItem,
  SubmitButton,
  normalizeInternalName,
  asyncValidate,
} from 'common/components/Forms'
import { createNewSurvey } from 'surveydesigner/newSurvey/actions'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Languages from 'utils/Languages'
import Strings from 'utils/Strings'

const templateTypes = ['BLANK', 'BIOPHYSICAL', 'COLLECT_EARTH', 'COLLECT_EARTH_IPCC']

const labelColSpan = 3
const fieldColSpan = 6
const fieldProps = { labelColSpan, fieldColSpan }

const NewSurveyParametersForm = (props) => {
  const { userGroups, handleSubmit, handleChange, values } = props

  const EMPTY_OPTION = (
    <option key="-1" value="" hidden>
      {L.l('forms.selectOne')}
    </option>
  )

  const templateTypeOptions = [EMPTY_OPTION].concat(
    templateTypes.map((type) => (
      <option key={type} value={type}>
        {L.l('survey.templateType.' + type)}
      </option>
    ))
  )

  const userGroupOptions = [EMPTY_OPTION].concat(
    userGroups.map((g) => (
      <option key={g.id} value={g.id}>
        {g.label}
      </option>
    ))
  )

  const languageItems = Languages.items()
  const mainLanguageCodes = ['en', 'fr', 'es']
  const mainLanguageItems = mainLanguageCodes.map((code) => ({ code, label: Languages.label(code) }))
  const otherLanguageItems = languageItems
    .filter((item) => !mainLanguageCodes.includes(item.code))
    .sort((a, b) => Strings.compare(a.label, b.label))

  const languageItemToOption = (item) => (
    <option key={item.code} value={item.code}>
      {item.label + ' (' + item.code + ')'}
    </option>
  )

  const languageOptions = [EMPTY_OPTION]
    .concat(mainLanguageItems.map(languageItemToOption))
    .concat([
      <option key="__separator__" disabled>
        ──────────
      </option>,
    ])
    .concat(otherLanguageItems.map(languageItemToOption))

  return (
    <Form onSubmit={handleSubmit}>
      <TextFormItem
        name="name"
        label={L.l('survey.name')}
        {...fieldProps}
        fieldColSpan={9}
        {...props}
        handleChange={(e) => {
          e.target.value = normalizeInternalName(e.target.value)
          handleChange(e)
        }}
      />
      <SelectFormItem name="templateType" label={L.l('survey.templateType')} {...fieldProps} {...props}>
        {templateTypeOptions}
      </SelectFormItem>
      {values.templateType && (
        <FormGroup row>
          <Label sm={2}></Label>
          <Col sm={10} className="new-survey-form__template-description-col">
            {L.l('survey.templateTypeDescription.' + values.templateType)}
          </Col>
        </FormGroup>
      )}
      <SelectFormItem name="defaultLanguageCode" label={L.l('survey.defaultLanguage')} {...fieldProps} {...props}>
        {languageOptions}
      </SelectFormItem>
      <SelectFormItem name="userGroupId" label={L.l('survey.userGroup')} {...fieldProps} {...props}>
        {userGroupOptions}
      </SelectFormItem>
      <Row>
        <Col sm={{ size: 1, offset: 5 }}>
          <SubmitButton {...props}>{L.l('common.new')}</SubmitButton>
        </Col>
      </Row>
    </Form>
  )
}

const mapStateToProps = (state) => {
  const { items: userGroups } = state.userGroups

  return {
    userGroups,
  }
}

const mapPropsToValues = (props) => {
  const defaultPublicGroup = props.userGroups.find((ug) => ug.name === User.DEFAULT_PUBLIC_GROUP_NAME)
  return {
    name: '',
    templateType: '',
    defaultLanguageCode: '',
    userGroupId: defaultPublicGroup ? defaultPublicGroup.id : '',
  }
}

export default compose(
  connect(mapStateToProps, { createNewSurvey }),
  withFormik({
    mapPropsToValues,
    validate: asyncValidate(ServiceFactory.surveyService.validateSurveyCreation.bind(ServiceFactory.surveyService)),
    handleSubmit: (values, { props }) => props.createNewSurvey(values),
  })
)(NewSurveyParametersForm)
