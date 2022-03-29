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
    DATA_ANALYZER: 'DATA_ANALYZER',
    DATA_CLEANER_LIMITED: 'DATA_CLEANER_LIMITED',
    OPERATOR: 'OPERATOR',
    VIEWER: 'VIEWER',
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

  _hasAtLeastRole(role) {
    const highestIndex = this._calculateHighestRoleIndex()
    const index = User.ROLES_HIERARCHY.indexOf(role)
    return highestIndex >= index
  }

  get canAccessUsersManagement() {
    return this.role === User.ROLE.ADMIN
  }

  canCreateRecords(roleInSurveyGroup) {
    switch (this.role) {
      case User.ROLE.VIEW:
      case User.ROLE.ENTRY_LIMITED:
        return false
      default:
        return this.canEditRecords(roleInSurveyGroup)
    }
  }

  canEditRecords(roleInSurveyGroup) {
    switch (this.role) {
      case User.ROLE.VIEW:
        return false
      default:
        if (roleInSurveyGroup === null) {
          return false
        }
        switch (roleInSurveyGroup) {
          case User.ROLE_IN_GROUP.OWNER:
          case User.ROLE_IN_GROUP.ADMINISTRATOR:
          case User.ROLE_IN_GROUP.SUPERVISOR:
          case User.ROLE_IN_GROUP.DATA_CLEANER_LIMITED:
          case User.ROLE_IN_GROUP.DATA_ANALYZER:
          case User.ROLE_IN_GROUP.OPERATOR:
            return true
          default:
            return false
        }
    }
  }

  canEditRecord({ record, userInGroupRole }) {
    const { step } = record
    const userOwnsRecord = record.ownerId === this.id
    switch (step) {
      case Workflow.STEPS.entry:
        return this._hasAtLeastRole(User.ROLE.ENTRY) || (this.role === User.ROLE.ENTRY_LIMITED && userOwnsRecord)
      case Workflow.STEPS.cleansing:
        return (
          (this._hasAtLeastRole(User.ROLE.CLEANSING) && userInGroupRole !== User.ROLE_IN_GROUP.DATA_CLEANER_LIMITED) ||
          userOwnsRecord
        )
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

  canEditQualifier(roleInSurvey) {
    return (
      this.role === User.ROLE.ADMIN ||
      roleInSurvey === User.ROLE_IN_GROUP.ADMINISTRATOR ||
      roleInSurvey === User.ROLE_IN_GROUP.OWNER
    )
  }

  canFilterRecordsBySummaryAttribute(attr, roleInSurvey) {
    const rootEntityDef = attr.rootEntity
    const isQualifier = rootEntityDef.qualifierAttributeDefinitions.find((qDef) => qDef.name === attr.name) != null
    return !isQualifier || this.canEditQualifier(roleInSurvey)
  }

  canEditRecordAttribute({ record, attributeDefinition }) {
    const { survey } = record
    const { userInGroupRole } = survey
    const { calculated, qualifier } = attributeDefinition
    return (
      this.canEditRecord({ record, userInGroupRole }) &&
      !calculated &&
      (!qualifier || this.canEditQualifier(userInGroupRole))
    )
  }

  canUnlockRecords() {
    return this.role === User.ROLE.ADMIN
  }

  canEditNotOwnedRecords() {
    return this.role === User.ROLE.ADMIN
  }

  canEditOnlyOwnedRecords() {
    return this.role === User.ROLE.ENTRY_LIMITED
  }

  canPromoteRecordWithErrors(roleInSurveyGroup) {
    return this.canChangeRecordOwner(roleInSurveyGroup)
  }

  canPromoteRecordsInBulk(roleInSurveyGroup) {
    return this.canPromoteRecordWithErrors(roleInSurveyGroup)
  }

  canDemoteRecordsInBulk(roleInSurveyGroup) {
    return this.canPromoteRecordWithErrors(roleInSurveyGroup)
  }

  canChangeRecordOwner(roleInSurveyGroup) {
    const mainRole = this.role
    switch (mainRole) {
      case User.ROLE.ADMIN:
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

  get canAccessDashboard() {
    return this.role !== User.ROLE.ENTRY_LIMITED
  }

  get canAccessMap() {
    return this.canAccessDashboard
  }

  canAccessSaiku({ roleInSurveyGroup }) {
    return this.canAccessDataCleansing({ roleInSurveyGroup })
  }

  canAccessDataCleansing({ roleInSurveyGroup }) {
    switch (this.role) {
      case User.ROLE.CLEANSING:
        return roleInSurveyGroup !== User.ROLE_IN_GROUP.DATA_CLEANER_LIMITED
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
