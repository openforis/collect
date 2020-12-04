import L from 'utils/Labels'

import { ValidationResultFlag } from './ValidationResultFlag'

export const getCardinalityErrors = ({ nodeDefinition, parentEntity }) => {
  const { id: nodeDefId } = nodeDefinition
  const minCount = parentEntity.childrenMinCountByDefinitionId[nodeDefId]
  const maxCount = parentEntity.childrenMaxCountByDefinitionId[nodeDefId]
  const validationMinCount = parentEntity.childrenMinCountValidationByDefinitionId[nodeDefId]
  const validationMaxCount = parentEntity.childrenMaxCountValidationByDefinitionId[nodeDefId]

  if (validationMinCount === ValidationResultFlag.ERROR) {
    return nodeDefinition.multiple
      ? L.l('dataManagement.dataEntry.validation.minCount', minCount)
      : L.l('dataManagement.dataEntry.validation.required')
  } else if (validationMaxCount === ValidationResultFlag.ERROR) {
    return L.l('dataManagement.dataEntry.validation.maxCount', maxCount)
  }
  return null
}

export const getCardinalityErrorsByChildDefName = ({ entity }) =>
  entity.definition.children.reduce((errorsByChildDefName, nodeDefinition) => {
    const errors = getCardinalityErrors({ nodeDefinition, parentEntity: entity })
    if (errors) {
      errorsByChildDefName[nodeDefinition.name] = errors
    }
    return errorsByChildDefName
  }, {})
