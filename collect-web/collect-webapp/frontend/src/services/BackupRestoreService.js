import AbstractService from './AbstractService';

export default class BackupRestoreService extends AbstractService {

    getLatestBackupInfo(surveyId) {
        return this.get('survey/'+ surveyId + '/backup/latest/info')
    }

    startFullBackup(surveyId) {
        return this.post('survey/' + surveyId + '/backup/start')
    }

    downloadBackupResult(surveyId) {
        this.downloadFile(this.BASE_URL + 'survey/' + surveyId + '/backup/result.collect-backup')
    }

    downloadLastBackup(surveyId) {
        this.downloadFile(this.BASE_URL + 'survey/' + surveyId + '/backup/latest.collect-backup')
    }
}
