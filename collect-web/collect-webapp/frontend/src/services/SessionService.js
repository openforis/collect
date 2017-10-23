import AbstractService from './AbstractService';

export default class SessionService extends AbstractService {

    fetchCurrentUser() {
        return this.get('session/user')
    }

    ping() {
        return this.get('session/ping')
    }

    invalidate() {
        return this.post('session/invalidate')
    }
    
}