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
        let userInGroup = userGroup.users.find(uig => uig.userId == this.id)
        return userInGroup == null ? userInGroup.role : null
    }

    canCreateRecords(userGroup) {
        const userInGroup = userGroup.users.find(uig => uig.userId === this.id)
        const role = userInGroup ? userInGroup.role : null
        if (role === null) {
            return false
        }
        switch(role) {
            case 'OWNER':
            case 'ADMINISTRATOR':
            case 'SUPERVISOR':
            case 'OPERATOR':
                return true
            default:
                return false
        }
    }

    canEditRecords(userGroup) {
        return this.canCreateRecords(userGroup)
    }

    canDeleteRecords(userGroup) {
        return this.canCreateRecords(userGroup)
    }

    canImportRecords(userGroup) {
        const userInGroup = userGroup.users.find(uig => uig.userId === this.id)
        const role = userInGroup ? userInGroup.role : null
        if (role === null) {
            return false
        }
        switch(role) {
            case 'OWNER':
            case 'ADMINISTRATOR':
            case 'SUPERVISOR':
                return true
            default:
                return false
        }
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