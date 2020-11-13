import Serializable from '../Serializable'
import { Node } from './Node'

class Field extends Serializable {
  value = null
  remarks = null

  get intValue() {
    return this.value == null ? null : parseInt(this.value.toString(), 10)
  }
}

export class Attribute extends Node {
  fields = []

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.fields = jsonObj.fields.reduce((acc, fieldJsonObj) => {
      const field = new Field()
      field.fillFromJSON(fieldJsonObj)
      acc.push(field)
      return acc
    }, [])
  }

  isAllFieldsEmpty() {
    if (!this.fields) {
      return true
    }
    return this.fields.every((field) => !field.value)
  }

  isEmpty() {
    if (!this.fields) {
      return true
    }
    const fields = this.fields
    const mandatoryFieldNames = this.definition.mandatoryFieldNames
    if (!mandatoryFieldNames) {
      return false
    }
    return this.definition.fieldNames.some((fieldName, index) => {
      const value = fields[index].value
      return mandatoryFieldNames.includes(fieldName) && (value === null || value === '')
    })
  }

  get value() {
    const $this = this
    return $this.isAllFieldsEmpty()
      ? null
      : $this.definition.fieldNames.reduce((valueAcc, fieldName, index) => {
          valueAcc[fieldName] = $this.fields[index].value
          return valueAcc
        }, {})
  }

  get humanReadableValue() {
    return this.fields && this.fields.length ? this.fields[0].value || '' : ''
  }

  set value(value) {
    if (this.fields == null) {
      this.fields = []
    }
    const fields = this.fields
    this.definition.fieldNames.forEach((fieldName, index) => {
      let field = fields[index]
      if (!field) {
        field = new Field()
        fields.push(field)
      }
      field.value = value ? value[fieldName] : null
    })
  }
}
