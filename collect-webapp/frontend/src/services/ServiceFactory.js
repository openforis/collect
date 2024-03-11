import ApplicationInfoService from './ApplicationInfoService'
import BackupRestoreService from './BackupRestoreService'
import CommandService from './CommandService'
import CodeListService from './CodeListService'
import GeoService from './GeoService'
import JobService from './JobService'
import RecordService from './RecordService'
import RecordFileService from './RecordFileService'
import SaikuService from './SaikuService'
import SessionService from './SessionService'
import SpeciesService from './SpeciesService'
import SurveyService from './SurveyService'
import UserService from './UserService'
import UserGroupService from './UserGroupService'

export default class ServiceFactory {
  static applicationInfoService = new ApplicationInfoService()
  static backupRestoreService = new BackupRestoreService()
  static commandService = new CommandService()
  static codeListService = new CodeListService()
  static geoService = new GeoService()
  static jobService = new JobService()
  static recordService = new RecordService()
  static recordFileService = new RecordFileService()
  static saikuService = new SaikuService()
  static sessionService = new SessionService()
  static speciesService = new SpeciesService()
  static surveyService = new SurveyService()
  static userService = new UserService()
  static userGroupService = new UserGroupService()
}
