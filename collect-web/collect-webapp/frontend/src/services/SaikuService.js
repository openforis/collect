import AbstractService from './AbstractService';
import { Survey } from '../model/Survey';

export default class SaikuService extends AbstractService {

    fetchReportingRepositoryInfo(survey) {
        const surveyName = survey.name
        return this.get('saiku/datasources/' + surveyName + '/info')
    }

    startReportingRepositoryGeneration(survey, language) {
        const surveyName = survey.name
        return this.post('saiku/datasources/' + surveyName + '/generate', {
            language: language
        })
    }
}