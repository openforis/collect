import AbstractService from './AbstractService';
import { Survey } from '../model/Survey';

export default class JobService extends AbstractService {

    fetch(jobId) {
        return this.get('job/' + jobId)
    }

    cancel(jobId) {
        return this.delete('job/' + jobId)
    }
}