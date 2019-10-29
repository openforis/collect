import React from 'react'
import { withFormik } from 'formik'

import Button from '@material-ui/core/Button'

import ServiceFactory from 'services/ServiceFactory'
import Forms, { TextFormItem, asyncValidate, handleValidationResponse } from 'common/components/Forms'
import Dialogs from 'common/components/Dialogs'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'
import Objects from '../../utils/Objects'

const PasswordChangeForm = props => {
    const { handleSubmit,
        isSubmitting,
        touched,
        errors } = props

    const fieldProps = { labelColSpan: 3, fieldColSpan: 9 }

    return <form onSubmit={handleSubmit} style={{ width: '700px' }}>
        <TextFormItem
            name="oldPassword"
            type="password"
            fullWidth={true}
            label={L.l('user.oldPassword')}
            {...fieldProps}
            {...props}
        />
        <TextFormItem
            name="newPassword"
            type="password"
            fullWidth={true}
            label={L.l('user.newPassword')}
            {...fieldProps}
            {...props}
        />
        <TextFormItem
            name="retypedPassword"
            type="password"
            fullWidth={true}
            label={L.l('user.retypedPassword')}
            {...fieldProps}
            {...props}
        />
        <div>
            <Button variant="contained" color="primary"
                disabled={Objects.isEmpty(touched) || Objects.isNotEmpty(errors) || isSubmitting} type="submit">
                {L.l('user.changePassword.change')}
            </Button>
        </div>
    </form>
}

const PasswordChangeFormEnanched = withFormik({
    validate: asyncValidate(ServiceFactory.userService.validatePasswordChange.bind(ServiceFactory.userService)),
    handleSubmit: (values, { setSubmitting, history }) =>
        ServiceFactory.userService.changePassword(values.oldPassword, values.newPassword)
            .then(r => {
                handleValidationResponse(r)
                if (r.statusOk) {
                    Dialogs.alert(L.l('global.info'), L.l('user.changePassword.passwordChanged'))
                    RouterUtils.navigateToHomePage(history)
                }
            })
            .finally(() => setSubmitting(false))
})(PasswordChangeForm)

const PasswordChangePage = props =>
    <PasswordChangeFormEnanched {...props} />

export default PasswordChangePage