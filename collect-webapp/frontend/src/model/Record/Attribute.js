import Serializable from '../Serializable'
import { Node } from './Node'

import Objects from 'utils/Objects'
import Validation from 'model/Validation'

class Field extends Serializable {
  value = null
  remarks = null

  get intValue() {
    return this.value == null ? null : parseInt(this.value.toString(), 10)
  }
}

export class Attribute extends Node {
  fields = []
  validationResults

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
    return this.fields.every((field) => Objects.isNullOrUndefined(field.value))
  }

  isEmpty() {
    if (!this.fields) {
      return true
    }
    const fields = this.fields
    const { fieldNames, mandatoryFieldNames } = this.definition

    const fieldNamesToEvaluate = mandatoryFieldNames ? mandatoryFieldNames : fieldNames
    return fieldNamesToEvaluate.some((_fieldName, index) => {
      const value = fields[index].value
      return value === null || value === ''
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
    if (!this.fields || this.fields.length === 0) return ''

    const firstFieldValue = this.fields[0].value
    return Objects.defaultIfNull(firstFieldValue, '')
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
      field.value = Objects.isNullOrUndefined(value) ? null : value[fieldName]
    })
  }

  get validation() {
    const { errors, warnings } = this.validationResults
    return new Validation({ errors, warnings })
  }
}
