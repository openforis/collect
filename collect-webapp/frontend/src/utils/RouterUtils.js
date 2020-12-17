import Constants from 'Constants'

class RouterUtils {
  static navigateToHomePage(history) {
    if (history) {
      RouterUtils._navigateToUrl(history, '')
    } else {
      window.location.assign(Constants.BASE_URL)
    }
  }

  static navigateToLoginPage(logout = false) {
    window.location.assign(Constants.BASE_URL + 'login.html?logout=true')
  }

  static navigateToPasswordChangePage(history) {
    RouterUtils._navigateToUrl(history, '/users/changepassword')
  }

  static navigateToDataManagementHomePage(history) {
    RouterUtils._navigateToUrl(history, '/datamanagement')
  }

  static navigateToRecordEditPage(history, recordId) {
    RouterUtils._navigateToUrl(history, `/datamanagement/${recordId}`)
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
    RouterUtils._navigateToUrl(history, '/surveydesigner/new')
  }

  static navigateToSurveyEditPage(history, surveyId) {
    RouterUtils._navigateToUrl(history, '/surveydesigner/' + surveyId)
  }

  static navigateToSurveyExportPage(history, surveyId) {
    RouterUtils._navigateToUrl(history, '/surveydesigner/export/' + surveyId)
  }

  static navigateToSurveyClonePage(history, surveyName) {
    RouterUtils._navigateToUrl(history, '/surveydesigner/clone/' + surveyName)
  }

  static navigateToUserGroupEditPage(history, userGroupId) {
    RouterUtils._navigateToUrl(history, '/usergroups/' + userGroupId)
  }

  static _navigateToUrl(history, url) {
    history.push(url)
  }

  static reloadPage() {
    window.location.reload()
  }
}

export default RouterUtils
