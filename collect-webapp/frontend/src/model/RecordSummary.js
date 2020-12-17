import Serializable from 'model/Serializable'

export default class RecordSummary extends Serializable {
  cleansingComplete
  createdBy
  creationDate
  entityCounts
  entryComplete
  errors
  id
  missing
  missingErrors
  missingWarnings
  modifiedBy
  modifiedDate
  owner
  rootEntityKeys
  skipped
  state
  step
  stepNumber
  stepSummaries
  summaryValues
  surveyId
  totalErrors
  versionId
  warnings

  get ownerId() {
    return this.owner ? this.owner.id : null
  }
}
