import L from 'utils/Labels'
import Validation from './Validation'

import { ValidationResultFlag } from './ValidationResultFlag'

export const getAttributeValidation = ({ parentEntity, attributeDefinition, attribute: attributeParam }) => {
  if (parentEntity) {
    let attribute = null
    if (!attributeDefinition.multiple) {
      attribute = parentEntity.getSingleChild(attributeDefinition.id)
    } else if (attributeParam) {
      attribute = attributeParam
    }
    if (attribute) {
      return attribute.validation
    }
  }
  return new Validation()
}

export const getCardinalityValidation = ({ nodeDefinition, parentEntity }) => {
  const { id: nodeDefId } = nodeDefinition
  const minCount = parentEntity.childrenMinCountByDefinitionId[nodeDefId]
  const maxCount = parentEntity.childrenMaxCountByDefinitionId[nodeDefId]
  const validationMinCount = parentEntity.childrenMinCountValidationByDefinitionId[nodeDefId]
  const validationMaxCount = parentEntity.childrenMaxCountValidationByDefinitionId[nodeDefId]

  const validation = new Validation()

  if (validationMinCount === ValidationResultFlag.ERROR) {
    validation.addError(
      nodeDefinition.multiple
        ? L.l('dataManagement.dataEntry.validation.minCount', minCount)
        : L.l('dataManagement.dataEntry.validation.required')
    )
  } else if (validationMaxCount === ValidationResultFlag.ERROR) {
    validation.addError(L.l('dataManagement.dataEntry.validation.maxCount', maxCount))
  }
  return validation
}

export const getCardinalityErrorMessageByChildDefName = ({ entity }) =>
  entity.definition.children.reduce((errorsByChildDefName, nodeDefinition) => {
    const cardinalityValidation = getCardinalityValidation({ nodeDefinition, parentEntity: entity })
    if (cardinalityValidation.hasErrors()) {
      errorsByChildDefName[nodeDefinition.name] = cardinalityValidation.errorMessage
    }
    return errorsByChildDefName
  }, {})
