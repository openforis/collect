import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form'
import Button from '@material-ui/core/Button';

import ServiceFactory from 'services/ServiceFactory';
import Forms from 'common/components/Forms';
import Dialogs from 'common/components/Dialogs'
import L from 'utils/Labels';
import RouterUtils from 'utils/RouterUtils';

const asyncValidate = (values /*, dispatch */) => {
    return ServiceFactory.userService.validatePasswordChange(values.oldPassword, values.newPassword, values.retypedPassword)
        .then(r => Forms.handleValidationResponse(r))
}

class PasswordChangePage extends Component {

    constructor(props) {
        super(props)

        this.submit = this.submit.bind(this)
    }

    submit(values) {
        return ServiceFactory.userService.changePassword(values.oldPassword, values.newPassword)
            .then(r => {
                Forms.handleValidationResponse(r)
                if (r.statusOk) {
                    Dialogs.alert(L.l('global.info'), L.l('user.changePassword.passwordChanged'))
                    RouterUtils.navigateToHomePage(this.props.history)
                }
            })
    }
    
    render() {
        const { handleSubmit, submitting, anyTouched, error } = this.props

        return (
            <form onSubmit={handleSubmit(this.submit)} style={{width: '700px'}}>
                <Field
                    name="oldPassword"
                    type="password"
                    component={Forms.renderFormItemInputField}
                    fullWidth={true}
                    labelColSpan={3}
                    fieldColSpan={9}
                    label={L.l('user.oldPassword')}
                    />
                <Field
                    name="newPassword"
                    type="password"
                    component={Forms.renderFormItemInputField}
                    fullWidth={true}
                    labelColSpan={3}
                    fieldColSpan={9}
                    label={L.l('user.newPassword')}
                    />
                <Field
                    name="retypedPassword"
                    type="password"
                    component={Forms.renderFormItemInputField}
                    fullWidth={true}
                    labelColSpan={3}
                    fieldColSpan={9}
                    label={L.l('user.retypedPassword')}
                    />
                {error && <div class="error">{error}</div>}
                <div>
                    <Button variant="contained" color="primary" disabled={!anyTouched || error || submitting} type="submit">
                        {L.l('user.changePassword.change')}
                    </Button>
                </div>
            </form>
        )
    }
}

export default reduxForm({ 
    form: 'passwordChangeForm',
    initialValues: {
        oldPassword: '',
        newPassword: '',
        retypedPassword: ''
    },
    asyncValidate,
    asyncBlurFields: ['oldPassword', 'newPassword', 'retypedPassword']
})(PasswordChangePage)