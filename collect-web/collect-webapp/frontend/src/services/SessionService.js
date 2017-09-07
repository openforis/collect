import AbstractService from './AbstractService';

export default class SessionService extends AbstractService {

    fetchCurrentUser() {
        return this.get('session/user')
    }
    
}