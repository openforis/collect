export default class Serializable {
  constructor(jsonObj) {
    if (jsonObj) {
      this.fillFromJSON(jsonObj)
    }
  }

  fillFromJSON(json, params = { skipFields: [] }) {
    const { skipFields } = params
    for (let propName in json) {
      if (skipFields && skipFields.includes(propName)) {
        continue
      }
      const newVal = json[propName]
      const oldVal = this[propName]
      if ((oldVal === null || oldVal === undefined) && newVal !== null && newVal !== undefined) {
        this[propName] = newVal
      }
    }
  }

  static createArrayFromJSON(jsonArr, itemClassName) {
    return jsonArr
      ? jsonArr.reduce((acc, jsonItem) => {
          const item = new itemClassName()
          item.fillFromJSON(jsonItem)
          acc.push(item)
          return acc
        })
      : []
  }
}
