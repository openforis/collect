import AbstractService from './AbstractService';
import Constants from '../utils/Constants';

export default class UserService extends AbstractService {

    fetchUsers() {
        var url = Constants.API_BASE_URL + 'user';
        return this.get(url)
    }

    update(user) {
        var url = Constants.API_BASE_URL + 'user';
        return this.post(url, user);
    }

    login(credentials) {
        var url = Constants.API_BASE_URL + 'login';
        return this.post(url, credentials);
    }

}