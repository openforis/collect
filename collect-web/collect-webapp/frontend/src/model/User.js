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

    determineRoleInGroup(userGroup, allUserGroups) {
        const userInGroup = this._findUserInGroupOrDescendants(userGroup, allUserGroups)
        return userInGroup == null ? userInGroup.role : null
    }

    canCreateRecords(userGroup, allUserGroups) {
        const userInGroup = this._findUserInGroupOrDescendants(userGroup, allUserGroups)
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

    canEditRecords(userGroup, allUserGroups) {
        return this.canCreateRecords(userGroup)
    }

    canDeleteRecords(userGroup, allUserGroups) {
        return this.canCreateRecords(userGroup)
    }

    canImportRecords(userGroup, allUserGroups) {
        const userInGroup = this._findUserInGroupOrDescendants(userGroup, allUserGroups)
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

    _findUserInGroupOrDescendants(userGroup, allUserGroups) {
        const stack = []
        stack.push(userGroup)
        while(stack.length > 0) {
            let currentUserGroup = stack.pop()
            let userInGroup = currentUserGroup.users.find(uig => uig.userId === this.id)
            if (userInGroup) {
                return userInGroup
            } else {
                let childrenGroups = userGroup.childrenGroupIds.map(id => allUserGroups.find(g => g.id === id))
                childrenGroups.forEach(g => stack.push(g))
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