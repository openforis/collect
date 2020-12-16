import Serializable from '../Serializable'

export class SurveyObject extends Serializable {
  id

  constructor(id) {
    super()
    this.id = id
  }
}
