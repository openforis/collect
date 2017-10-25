import Serializable from './Serializable'

export default class User extends Serializable {

    enabled
    id
    role
    username
    
    constructor(jsonData) {
        super()
        this.fillFromJSON(jsonData)
    }

    determineRoleInGroup(userGroup) {
        let userInGroup = userGroup.users.find(u => u.user.id == this.id)
        return userInGroup == null ? userInGroup.role : null
    }

    get canAccessSurveyDesigner() {
        switch(this.role) {
            case 'ADMIN':
                return true
            default:
                return false
        }
    }

    get canAccessUsersManagement() {
        return this.role === 'ADMIN'
    }

    get canAccessSaiku() {
        return this.role === 'ADMIN'
    }

    get canAccessDataCleansing() {
        switch(this.role) {
            case 'CLEANSING':
            case 'ANALYSIS':
            case 'ADMIN':
                return true
            default:
                return false
        }
    }


}