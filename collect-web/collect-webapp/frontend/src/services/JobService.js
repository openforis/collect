import AbstractService from './AbstractService';
import { Survey } from '../model/Survey';

export default class JobService extends AbstractService {

    fetchById(jobId) {
        return this.get('job/' + jobId)
    }
    
}