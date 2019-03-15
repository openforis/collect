import AbstractService from './AbstractService';

export default class ApplicationInfoService extends AbstractService {

    fetchInfo() {
        return this.get('completeinfo')
    }
}