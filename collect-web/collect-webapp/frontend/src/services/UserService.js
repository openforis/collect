import AbstractService from './AbstractService';

export default class UserService extends AbstractService {

    fetchUsers() {
        return this.get('user')
    }

    save(user) {
        return this.post('user', user);
    }

    deleteUsers(loggedUserId, userIds) {
        return this.delete('user', {
            loggedUserId: loggedUserId,
            userIds: userIds
        })
    }

    login(credentials) {
        return this.post('login', credentials);
    }

}