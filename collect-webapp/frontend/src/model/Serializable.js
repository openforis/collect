export default class Serializable {
  constructor(jsonObj) {
    if (jsonObj) {
      this.fillFromJSON(jsonObj)
    }
  }

  fillFromJSON(jsonObj, params = { skipFields: [] }) {
    const { skipFields } = params
    for (var propName in jsonObj) {
      if (skipFields && skipFields.includes(propName)) {
        continue
      }
      let newVal = jsonObj[propName]
      let oldVal = this[propName]
      if ((oldVal === null || oldVal === undefined) && newVal !== null && newVal !== undefined) {
        this[propName] = newVal
      }
    }
  }

  static createArrayFromJSON(jsonArr, itemClassName) {
    let result = []
    for (var i = 0; i < jsonArr.length; i++) {
      var itemJsonObj = jsonArr[i]
      var item = new itemClassName()
      item.fillFromJSON(itemJsonObj)
      result.push(item)
    }
    return result
  }
}
