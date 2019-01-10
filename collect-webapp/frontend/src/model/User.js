import Serializable from './Serializable'
import Arrays from 'utils/Arrays'
import Workflow from './Workflow';

export default class User extends Serializable {

    static ROLE = {
        VIEW: 'VIEW',
        ENTRY_LIMITED: 'ENTRY_LIMITED',
        ENTRY: 'ENTRY',
        CLEANSING: 'CLEANSING',
        ANALYSIS: 'ANALYSIS',
        DESIGN: 'DESIGN',
        ADMIN: 'ADMIN'
    }

    static ROLE_IN_GROUP = {
        OWNER: 'OWNER', 
        ADMINISTRATOR: 'ADMINISTRATOR',
        SUPERVISOR: 'SUPERVISOR',
        OPERATOR: 'OPERATOR',
        VIEWER: 'VIEWER',
        DATA_ANALYZER: 'DATA_ANALYZER'
    }

    static USER_GROUP_JOIN_STATUS = {
        ACCEPTED: 'ACCEPTED',
        PENDING: 'PENDING',
        REJECTED: 'REJECTED'
    }

    enabled
    id
    role
    username
    
    constructor(jsonData) {
        super()
        this.fillFromJSON(jsonData)
    }

    canCreateRecords(roleInSurveyGroup) {
        const mainRole = this.role
        switch(mainRole) {
            case User.ROLE.VIEW:
            case User.ROLE.ENTRY_LIMITED:
                return false
            default:
                if (roleInSurveyGroup === null) {
                    return false
                }
                switch(roleInSurveyGroup) {
                    case User.ROLE_IN_GROUP.OWNER:
                    case User.ROLE_IN_GROUP.ADMINISTRATOR:
                    case User.ROLE_IN_GROUP.SUPERVISOR:
                    case User.ROLE_IN_GROUP.DATA_ANALYZER:
                    case User.ROLE_IN_GROUP.OPERATOR:
                        return true
                    default:
                        return false
                }
        }
    }

    canEditRecords(roleInSurveyGroup) {
        return this.canCreateRecords(roleInSurveyGroup)
    }

    canDeleteRecords(roleInSurveyGroup, records) {
        const canDeleteRecordsInGeneral = this.canCreateRecords(roleInSurveyGroup)
        if (! canDeleteRecordsInGeneral) {
            return false
        }
        switch (this.role) {
            case User.ROLE.ENTRY:
                return ! Arrays.contains(records, r => r.step !== Workflow.STEPS.entry.code || r.ownerId !== this.id)
            default:
                return true
        }
    }

    canImportRecords(roleInSurveyGroup) {
        if (roleInSurveyGroup === null) {
            return false
        }
        switch(roleInSurveyGroup) {
            case User.ROLE_IN_GROUP.OWNER:
            case User.ROLE_IN_GROUP.ADMINISTRATOR:
            case User.ROLE_IN_GROUP.SUPERVISOR:
            case User.ROLE_IN_GROUP.DATA_ANALYZER:
                return true
            default:
                return false
        }
    }

    canPromoteRecordsInBulk(roleInSurveyGroup) {
        return this.canChangeRecordOwner(roleInSurveyGroup)
    }

    canDemoteRecordsInBulk(roleInSurveyGroup) {
        return this.canPromoteRecordsInBulk(roleInSurveyGroup)
    }

    canChangeRecordOwner(roleInSurveyGroup) {
        const mainRole = this.role
        switch(mainRole) {
            case 'ADMIN':
                return true
            case User.ROLE.VIEW:
            case User.ROLE.ENTRY:
            case User.ROLE.ENTRY_LIMITED:
                return false
            default:
                if (roleInSurveyGroup === null) {
                    return false
                }
                switch(roleInSurveyGroup) {
                    case User.ROLE_IN_GROUP.OWNER:
                    case User.ROLE_IN_GROUP.ADMINISTRATOR:
                    case User.ROLE_IN_GROUP.SUPERVISOR:
                    case User.ROLE_IN_GROUP.DATA_ANALYZER:
                        return true
                    default:
                        return false
                }
        }
    }
    
    get canAccessSurveyDesigner() {
        switch(this.role) {
        	case User.ROLE.ADMIN:
            case User.ROLE.DESIGN:
                return true
            default:
                return false
        }
    }

    canChangeSurveyUserGroup(roleInSurveyGroup) {
        switch(this.role) {
            case User.ROLE.ADMIN:
                return true
            case User.ROLE.DESIGN:
                if (roleInSurveyGroup === null) {
                    return false
                }
                switch(roleInSurveyGroup) {
                    case User.ROLE_IN_GROUP.OWNER:
                    case User.ROLE_IN_GROUP.ADMINISTRATOR:
                        return true
                    default:
                        return false
                }
            default:
                return false
        }
    }

    get canAccessUsersManagement() {
        return this.role === User.ROLE.ADMIN
    }

    get canAccessSaiku() {
        return this.canAccessDataCleansing
    }

    get canAccessDataCleansing() {
        switch(this.role) {
            case User.ROLE.CLEANSING:
            case User.ROLE.ANALYSIS:
            case User.ROLE.DESIGN:
            case User.ROLE.ADMIN:
                return true
            default:
                return false
        }
    }

    get canAccessBackupRestore() {
        return this.role === User.ROLE.ADMIN
    }

    canFilterRecordsBySummaryAttribute(attr, roleInSurvey) {
        const rootEntityDef = attr.rootEntity
        const isQualifier = rootEntityDef.qualifierAttributeDefinitions.find(qDef => qDef.name === attr.name) != null
        return ! isQualifier 
        || this.role === User.ROLE.ADMIN 
        || roleInSurvey === User.ROLE_IN_GROUP.ADMINISTRATOR 
        || roleInSurvey === User.ROLE_IN_GROUP.OWNER

    }

    canUnlockRecords() {
        return this.role === User.ROLE.ADMIN
    }
}