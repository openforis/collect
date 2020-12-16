import Serializable from 'model/Serializable'

export class Unit extends Serializable {
  id
  conversionFactor
  abbreviation
  label

  constructor(survey) {
    super()
    this.survey = survey
  }
}
