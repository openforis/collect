import AbstractService from './AbstractService';
import Constants from '../utils/Constants';

export default class UserService extends AbstractService {

    constructor() {
        super();
    }

    fetchUsers() {
        var url = Constants.API_BASE_URL + 'user';
        return this.getJson(url)
    }

    updateUser(user) {
        var url = Constants.API_BASE_URL + 'user';
        return this.postJson(url, user);
    }


}