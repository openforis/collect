import React, { Component } from 'react';
import { Formik } from 'formik';

import Button from '@material-ui/core/Button';

import ServiceFactory from 'services/ServiceFactory';
import Forms, { TextFormItem } from 'common/components/Forms';
import Dialogs from 'common/components/Dialogs'
import L from 'utils/Labels';
import RouterUtils from 'utils/RouterUtils';
import Objects from '../../utils/Objects'

const PasswordChangePage = props => {
    const { history } = props

    const fieldProps = { labelColSpan: 3, fieldColSpan: 9 }

    return (
        <Formik
            onSubmit={(values, { setSubmitting }) =>
                ServiceFactory.userService.changePassword(values.oldPassword, values.newPassword)
                    .then(r => {
                        Forms.handleValidationResponse(r)
                        if (r.statusOk) {
                            Dialogs.alert(L.l('global.info'), L.l('user.changePassword.passwordChanged'))
                            RouterUtils.navigateToHomePage(history)
                        }
                    })
                    .finally(() => setSubmitting(false))
            }
            validate={values =>
                ServiceFactory.userService.validatePasswordChange(values.oldPassword, values.newPassword, values.retypedPassword)
                    .then(r => Forms.handleValidationResponse(r))}>
            {formProps => {
                const {
                    errors,
                    handleSubmit,
                    isSubmitting,
                    touched,
                } = formProps

                return <form onSubmit={handleSubmit} style={{ width: '700px' }}>
                    <TextFormItem
                        name="oldPassword"
                        type="password"
                        fullWidth={true}
                        label={L.l('user.oldPassword')}
                        {...fieldProps}
                        {...formProps}
                    />
                    <TextFormItem
                        name="newPassword"
                        type="password"
                        fullWidth={true}
                        label={L.l('user.newPassword')}
                        {...fieldProps}
                        {...formProps}
                    />
                    <TextFormItem
                        name="retypedPassword"
                        type="password"
                        fullWidth={true}
                        label={L.l('user.retypedPassword')}
                        {...fieldProps}
                        {...formProps}
                    />
                    <div>
                        <Button variant="contained" color="primary"
                            disabled={Objects.isEmpty(touched) || Objects.isNotEmpty(errors) || isSubmitting} type="submit">
                            {L.l('user.changePassword.change')}
                        </Button>
                    </div>
                </form>
            }}
        </Formik>
    )
}

export default PasswordChangePage