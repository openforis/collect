import CommandService from './CommandService'
import CodeListService from './CodeListService'
import RecordService from './RecordService'
import UserService from './UserService'

export default class ServiceFactory {

    static commandService = new CommandService()
    static codeListService = new CodeListService()
    static recordService = new RecordService()
    static userService = new UserService()
}