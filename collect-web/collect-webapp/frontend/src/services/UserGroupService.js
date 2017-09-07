import AbstractService from './AbstractService';

export default class UserGroupService extends AbstractService {

    fetchAllAvailableGroups() {
        return this.get('usergroup');
    }

    save(userGroup) {
        return this.post('usergroup', userGroup);
    }
}

