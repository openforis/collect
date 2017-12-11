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
  version
  warnings

  constructor(jsonObj) {
    super(jsonObj)
  }

  get ownerId() {
    return this.owner ? this.owner.id : null
  }
}