import AbstractService from './AbstractService';
import { Survey } from '../model/Survey';

export default class SurveyService extends AbstractService {

    fetchById(surveyId) {
        return this.get('survey/' + surveyId).then(res => new Survey(res))
    }

    createNewSurvey(name, templateType, defaultLanguageCode, userGroupId) {
        return this.post('survey', {
            name: name,
            templateType: templateType,
            defaultLanguageCode:  defaultLanguageCode,
            userGroupId: userGroupId
        })
    }

    validateSurveyCreation(name, templateType, defaultLanguageCode, userGroupId) {
        return this.post('survey/validatecreation', {
            name: name,
            templateType: templateType,
            defaultLanguageCode:  defaultLanguageCode,
            userGroupId: userGroupId
        })
    }

    changeUserGroup(surveyId, userGroupId, loggedUserId) {
        return this.post('survey/changeusergroup/' + surveyId, {
            userGroupId: userGroupId,
            loggedUserId: loggedUserId
        }).then(res => res)
    }
    
    fetchAllSummaries() {
        return this.get('survey')
    }

    uploadSurveyFile(file) {
        return this.postFormData('survey/prepareimport', {
            file: file
        })
    }

    startSurveyFileImport(name, userGroupId) {
        return this.post('survey/startimport', {
            name: name,
            userGroupId: userGroupId
        })
    }
}