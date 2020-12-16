import Serializable from 'model/Serializable'

export class SpatialReferenceSystem extends Serializable {
  id
  label
  description

  constructor(survey) {
    super()
    this.survey = survey
  }
}
