export default class RouterUtils {

    static navigateToSurveyEditPage(history, surveyId) {
        RouterUtils._navigateToUrl(history, '/surveydesigner/surveys/' + surveyId)
    }

    static navigateToDataManagementHomePage(history) {
        RouterUtils._navigateToUrl(history, '/datamanagement')
    }

    static navigateToRecordEditPage(history, recordId) {
        RouterUtils._navigateToUrl(history, '/datamanagement/' + recordId)        
    }

    static navigateToRecordCsvExportPage(history) {
        RouterUtils._navigateToUrl(history, '/datamanagement/csvexport')
    }

    static navigateToRecordBackupPage(history) {
        RouterUtils._navigateToUrl(history, '/datamanagement/backup')
    }

    static navigateToRecordBackupImportPage(history) {
        RouterUtils._navigateToUrl(history, '/datamanagement/backupimport')
    }

    static navigateToRecordCsvImportPage(history) {
        RouterUtils._navigateToUrl(history, '/datamanagement/csvimport')
    }

    static navigateToNewSurveyPage(history) {
        RouterUtils._navigateToUrl(history, '/surveydesigner/newsurvey')
    }

    static navigateToUserGroupEditPage(history, userGroupId) {
        RouterUtils._navigateToUrl(history, '/usergroups/' + userGroupId)
    }

    static _navigateToUrl(history, url) {
        history.push(url)
    }

}