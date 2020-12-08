import Serializable from './Serializable'
import Arrays from 'utils/Arrays'
import Workflow from './Workflow'

export default class User extends Serializable {
  static DEFAULT_ADMIN_NAME = 'admin'
  static DEFAULT_PUBLIC_GROUP_NAME = 'default_public_group'

  static ROLE = {
    VIEW: 'VIEW',
    ENTRY_LIMITED: 'ENTRY_LIMITED',
    ENTRY: 'ENTRY',
    CLEANSING: 'CLEANSING',
    ANALYSIS: 'ANALYSIS',
    DESIGN: 'DESIGN',
    ADMIN: 'ADMIN',
  }

  static ROLES_HIERARCHY = [
    User.ROLE.VIEW,
    User.ROLE.ENTRY_LIMITED,
    User.ROLE.ENTRY,
    User.ROLE.CLEANSING,
    User.ROLE.ANALYSIS,
    User.ROLE.DESIGN,
    User.ROLE.ADMIN,
  ]

  static ROLE_IN_GROUP = {
    OWNER: 'OWNER',
    ADMINISTRATOR: 'ADMINISTRATOR',
    SUPERVISOR: 'SUPERVISOR',
    OPERATOR: 'OPERATOR',
    VIEWER: 'VIEWER',
    DATA_ANALYZER: 'DATA_ANALYZER',
  }

  static USER_GROUP_JOIN_STATUS = {
    ACCEPTED: 'ACCEPTED',
    PENDING: 'PENDING',
    REJECTED: 'REJECTED',
  }

  enabled
  id
  role
  username

  constructor(jsonData) {
    super()
    this.fillFromJSON(jsonData)
  }

  _calculateHighestRoleIndex() {
    let max = -1
    this.roles.forEach((role) => {
      const index = User.ROLES_HIERARCHY.indexOf(role)
      if (index > max) {
        max = index
      }
    })
    return max
  }

  _hasEffectiveRole(role) {
    const highestIndex = this._calculateHighestRoleIndex()
    const index = User.ROLES_HIERARCHY.indexOf(role)
    return highestIndex >= index
  }

  get canAccessUsersManagement() {
    return this.role === User.ROLE.ADMIN
  }

  canCreateRecords(roleInSurveyGroup) {
    const mainRole = this.role
    switch (mainRole) {
      case User.ROLE.VIEW:
      case User.ROLE.ENTRY_LIMITED:
        return false
      default:
        if (roleInSurveyGroup === null) {
          return false
        }
        switch (roleInSurveyGroup) {
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

  canEditRecord(record) {
    const { step } = record

    switch (step) {
      case Workflow.STEPS.entry:
        return this._hasEffectiveRole(User.ROLE.ENTRY_LIMITED)
      case Workflow.STEPS.cleansing:
        return this._hasEffectiveRole(User.ROLE.CLEANSING)
      case Workflow.STEPS.analysis:
      default:
        return false
    }
  }

  canDeleteRecords(roleInSurveyGroup, records) {
    const canDeleteRecordsInGeneral = this.canCreateRecords(roleInSurveyGroup)
    if (!canDeleteRecordsInGeneral) {
      return false
    }
    switch (this.role) {
      case User.ROLE.ENTRY:
        return !Arrays.contains(records, (r) => r.step !== Workflow.STEPS.entry || r.ownerId !== this.id)
      default:
        return true
    }
  }

  canImportRecords(roleInSurveyGroup) {
    if (roleInSurveyGroup === null) {
      return false
    }
    switch (roleInSurveyGroup) {
      case User.ROLE_IN_GROUP.OWNER:
      case User.ROLE_IN_GROUP.ADMINISTRATOR:
      case User.ROLE_IN_GROUP.SUPERVISOR:
      case User.ROLE_IN_GROUP.DATA_ANALYZER:
        return true
      default:
        return false
    }
  }

  canFilterRecordsBySummaryAttribute(attr, roleInSurvey) {
    const rootEntityDef = attr.rootEntity
    const isQualifier = rootEntityDef.qualifierAttributeDefinitions.find((qDef) => qDef.name === attr.name) != null
    return (
      !isQualifier ||
      this.role === User.ROLE.ADMIN ||
      roleInSurvey === User.ROLE_IN_GROUP.ADMINISTRATOR ||
      roleInSurvey === User.ROLE_IN_GROUP.OWNER
    )
  }

  canUnlockRecords() {
    return this.role === User.ROLE.ADMIN
  }

  canEditNotOwnedRecords() {
    return this.role === User.ROLE.ADMIN
  }

  canPromoteRecordsInBulk(roleInSurveyGroup) {
    return this.canChangeRecordOwner(roleInSurveyGroup)
  }

  canDemoteRecordsInBulk(roleInSurveyGroup) {
    return this.canPromoteRecordsInBulk(roleInSurveyGroup)
  }

  canChangeRecordOwner(roleInSurveyGroup) {
    const mainRole = this.role
    switch (mainRole) {
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
        switch (roleInSurveyGroup) {
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

  get canAccessSaiku() {
    return this.canAccessDataCleansing
  }

  get canAccessDataCleansing() {
    switch (this.role) {
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

  get canAccessSurveyDesigner() {
    switch (this.role) {
      case User.ROLE.ADMIN:
      case User.ROLE.DESIGN:
        return true
      default:
        return false
    }
  }

  canChangeSurveyUserGroup(roleInSurveyGroup) {
    switch (this.role) {
      case User.ROLE.ADMIN:
        return true
      case User.ROLE.DESIGN:
        if (roleInSurveyGroup === null) {
          return false
        }
        switch (roleInSurveyGroup) {
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
}
