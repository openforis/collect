import Serializable from 'model/Serializable'
import { CodeListItem } from './CodeListItem'

export class CodeList extends Serializable {
  survey
  id
  name
  items = []
  hierarchycal

  constructor(survey) {
    super()
    this.survey = survey
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.items = jsonObj.items.map((itemJsonObj) => {
      return new CodeListItem(itemJsonObj)
    })
  }
}
