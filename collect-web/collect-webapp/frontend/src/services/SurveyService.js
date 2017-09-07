import AbstractService from './AbstractService';
import { Survey } from '../model/Survey';

export default class SurveyService extends AbstractService {

    fetchById(surveyId) {
        return this.get('survey/' + surveyId).then(res => new Survey(res))
    }
    
    fetchAllSummaries() {
        return this.get('survey')
    }
}