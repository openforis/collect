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

    determineRoleInGroup(group) {
        const userInGroup = this._findUserInGroupOrDescendants(group)
        return userInGroup == null ? null : userInGroup.role
    }

    canCreateRecords(group) {
        const role = this.determineRoleInGroup(group)
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

    canEditRecords(group) {
        return this.canCreateRecords(group)
    }

    canDeleteRecords(group) {
        return this.canCreateRecords(group)
    }

    canImportRecords(group) {
        const role = this.determineRoleInGroup(group)
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

    canChangeRecordOwner(group) {
        const role = this.determineRoleInGroup(group)
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
    
    _findUserInGroupOrDescendants(group) {
        const stack = []
        stack.push(group)
        while(stack.length > 0) {
            let currentGroup = stack.pop()
            let userInGroup = currentGroup.users.find(uig => uig.userId === this.id)
            if (userInGroup) {
                return userInGroup
            } else {
                currentGroup.children.forEach(g => stack.push(g))
            }
        }
        return null
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