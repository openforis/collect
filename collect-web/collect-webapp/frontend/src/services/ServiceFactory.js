import ApplicationInfoService from './ApplicationInfoService'
import BackupRestoreService from './BackupRestoreService'
import CommandService from './CommandService'
import CodeListService from './CodeListService'
import JobService from './JobService'
import QueryService from './QueryService'
import RecordService from './RecordService'
import SaikuService from './SaikuService'
import SessionService from './SessionService'
import SurveyService from './SurveyService'
import UserService from './UserService'
import UserGroupService from './UserGroupService'

export default class ServiceFactory {

    static applicationInfoService = new ApplicationInfoService()
    static backupRestoreService = new BackupRestoreService()
    static commandService = new CommandService()
    static codeListService = new CodeListService()
    static jobService = new JobService()
    static queryService = new QueryService()
    static recordService = new RecordService()
    static saikuService = new SaikuService()
    static sessionService = new SessionService()
    static surveyService = new SurveyService()
    static userService = new UserService()
    static userGroupService = new UserGroupService()
}