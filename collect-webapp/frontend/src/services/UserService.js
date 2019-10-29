import AbstractService from './AbstractService'

export default class UserService extends AbstractService {

    fetchUsers() {
        return this.get('user')
    }

    save(user) {
        return this.post('user', user)
    }

    validate(user) {
        return this.post('user/validate', user)
    }

    deleteUsers(loggedUserId, userIds) {
        return this.delete('user', {
            loggedUserId,
            userIds
        })
    }

    login(credentials) {
        return this.post('login', credentials);
    }

    validatePasswordChange({ oldPassword, newPassword, retypedPassword }) {
        return this.post('user/validatepasswordchange', {
            oldPassword,
            newPassword,
            retypedPassword
        })
    }

    changePassword(oldPassword, newPassword) {
        return this.post('user/changepassword', {
            oldPassword,
            newPassword
        })
    }

}