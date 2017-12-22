import Serializable from './Serializable'
import Arrays from 'utils/Arrays'

export default class User extends Serializable {

    static ROLES = ['VIEW', 'ENTRY_LIMITED', 'ENTRY', 'CLEANSING', 'ANALYSIS', 'ADMIN']

    enabled
    id
    role
    username
    
    constructor(jsonData) {
        super()
        this.fillFromJSON(jsonData)
    }

    determineRoleInGroup(group) {
        const userInGroup = this.findUserInGroupOrDescendants(group)
        return userInGroup == null ? null : userInGroup.role
    }

    canCreateRecords(group) {
        const mainRole = this.role
        switch(mainRole) {
            case 'VIEW':
            case 'ENTRY_LIMITED':
                return false
            default:
                const roleInGroup = this.determineRoleInGroup(group)
                if (roleInGroup === null) {
                    return false
                }
                switch(roleInGroup) {
                    case 'OWNER':
                    case 'ADMINISTRATOR':
                    case 'SUPERVISOR':
                    case 'OPERATOR':
                        return true
                    default:
                        return false
                }
        }
    }

    canEditRecords(group) {
        return this.canCreateRecords(group)
    }

    canDeleteRecords(group, records) {
        const canDeleteRecordsInGeneral = this.canCreateRecords(group)
        if (! canDeleteRecordsInGeneral) {
            return false
        }
        switch (this.role) {
            case 'ENTRY':
                return ! Arrays.contains(records, r => r.step !== 'ENTRY' || r.ownerId !== this.id)
            default:
                return true
                
        }
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

    canPromoteRecordsInBulk(group) {
        return this.canChangeRecordOwner(group)
    }

    canDemoteRecordsInBulk(group) {
        return this.canPromoteRecordsInBulk(group)
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
    
    findUserInGroupOrDescendants(group) {
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

    get canAccessBackupRestore() {
        return this.role === 'ADMIN'
    }
}