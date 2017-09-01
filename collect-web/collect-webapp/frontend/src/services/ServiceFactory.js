import CommandService from './CommandService'
import CodeListService from './CodeListService'

export default class ServiceFactory {

    static commandService = new CommandService()
    static codeListService = new CodeListService()

}