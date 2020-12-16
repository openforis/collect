import Strings from 'utils/Strings'

import { SurveyObject } from './SurveyObject'

export class NodeDefinition extends SurveyObject {
  survey
  parent
  name
  label
  numberLabel
  headingLabel
  description
  multiple
  sinceVersionId
  deprecatedVersionId
  alwaysRelevant
  hideWhenNotRelevant
  width
  labelWidth

  static Types = {
    ENTITY: 'ENTITY',
    ATTRIBUTE: 'ATTRIBUTE',
  }

  static NUMBER_LABEL_SUFFIX = '.'

  constructor(id, survey, parent) {
    super(id)
    this.survey = survey
    this.parent = parent
  }

  visitAncestors(visitor) {
    let currentParent = this.parent
    while (currentParent != null) {
      visitor(currentParent)
      currentParent = currentParent.parent
    }
  }

  findAncestor(predicate) {
    let currentParent = this.parent
    while (currentParent != null) {
      if (predicate(currentParent)) {
        return currentParent
      }
      currentParent = currentParent.parent
    }
    return null
  }

  get rootEntity() {
    return this.findAncestor((ancestor) => !ancestor.parent)
  }

  get parentMultipleEntity() {
    return this.findAncestor((ancestor) => ancestor.multiple)
  }

  get ancestorIds() {
    const ancestorIds = []
    this.visitAncestors((ancestor) => ancestorIds.push(ancestor.id))
    return ancestorIds
  }

  get ancestorAndSelfIds() {
    return [...this.ancestorIds, this.id]
  }

  isDescendantOf(entityDef) {
    return this.ancestorIds.includes(entityDef.id)
  }

  get single() {
    return !this.multiple
  }

  get labelOrName() {
    return Strings.isNotBlank(this.label) ? this.label : this.name
  }

  get fullLabelOrName() {
    return `${this.numberLabel ? `${this.numberLabel}${NodeDefinition.NUMBER_LABEL_SUFFIX} ` : ''}${this.labelOrName}`
  }

  get sinceVersion() {
    return this.sinceVersionId ? this.survey.getVersionById(this.sinceVersionId) : null
  }

  get deprecatedVersion() {
    return this.deprecatedVersionId ? this.survey.getVersionById(this.deprecatedVersionId) : null
  }

  isInVersion(version) {
    if (version === null) {
      return true
    }
    // check since version
    if (this.sinceVersionId) {
      if (this.sinceVersionId === version.id) {
        return true
      }
      if (this.sinceVersion.date > version.date) {
        return false
      }
    }
    // check deprecated version
    if (this.deprecatedVersionId) {
      if (this.deprecatedVersionId === version.id || this.deprecatedVersion.date < version.date) {
        return false
      }
    }
    return true
  }
}
