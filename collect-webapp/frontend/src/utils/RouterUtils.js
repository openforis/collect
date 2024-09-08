import Constants from 'Constants'

class RouterUtils {
  static navigateToHomePage(navigate) {
    if (navigate) {
      RouterUtils._navigateToUrl(navigate, '')
    } else {
      window.location.assign(Constants.BASE_URL)
    }
  }

  static navigateToLoginPage(_logout = false) {
    window.location.assign(Constants.BASE_URL + 'login.html?logout=true')
  }

  static navigateToPasswordChangePage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/users/changepassword')
  }

  static navigateToDataManagementHomePage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/datamanagement')
  }

  static navigateToRecordEditPage(navigate, recordId) {
    RouterUtils._navigateToUrl(navigate, `/datamanagement/${recordId}`)
  }

  static navigateToRecordCsvExportPage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/datamanagement/csvexport')
  }

  static navigateToRecordBackupPage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/datamanagement/xmlexport')
  }

  static navigateToRecordBackupImportPage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/datamanagement/backupimport')
  }

  static navigateToRecordCsvImportPage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/datamanagement/csvimport')
  }

  static navigateToRandomGridGenerationPage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/datamanagement/randomgrid')
  }

  static navigateToNewSurveyPage(navigate) {
    RouterUtils._navigateToUrl(navigate, '/surveydesigner/new')
  }

  static navigateToSurveyEditPage(navigate, surveyId) {
    RouterUtils._navigateToUrl(navigate, '/surveydesigner/' + surveyId)
  }

  static navigateToSurveyExportPage(navigate, surveyId) {
    RouterUtils._navigateToUrl(navigate, '/surveydesigner/export/' + surveyId)
  }

  static navigateToSurveyClonePage(navigate, surveyName) {
    RouterUtils._navigateToUrl(navigate, '/surveydesigner/clone/' + surveyName)
  }

  static navigateToUserGroupEditPage(navigate, userGroupId) {
    RouterUtils._navigateToUrl(navigate, '/usergroups/' + userGroupId)
  }

  static _navigateToUrl(navigate, url) {
    navigate(url)
  }

  static reloadPage() {
    window.location.reload()
  }
}

export default RouterUtils
