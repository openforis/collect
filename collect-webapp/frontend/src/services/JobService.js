import AbstractService from './AbstractService';

export default class JobService extends AbstractService {

    fetch(jobId) {
        return this.get('job/' + jobId)
    }

    cancel(jobId) {
        return this.delete('job/' + jobId)
    }
}