import Objects from 'utils/Objects'

const validateRequired = (value) => (Objects.isEmpty(value) ? 'validation.required_field' : null)
const validateGreaterThan = (minValue) => (value) =>
  Number(value) <= minValue ? { messageKey: 'validation.mustBeGreaterThan', messageArgs: [minValue] } : null
const validateLessThan = (maxValue) => (value) =>
  Number(value) > maxValue ? { messageKey: 'validation.mustBeLowerThan', messageArgs: [maxValue] } : null

const validateField = ({ object, fieldKey, validationsByField }) => {
  const value = object[fieldKey]
  const validations = validationsByField[fieldKey] ?? []
  const validationResults = validations.reduce((acc, validation) => {
    const validationResult = validation(value)
    if (validationResult) {
      acc.push(validationResult)
    }
    return acc
  }, [])
  return validationResults.length > 0 ? validationResults : null
}

const validateFields = ({ object, validationsByField }) =>
  Object.keys(object).reduce((acc, fieldKey) => {
    const validationResults = validateField({ object, fieldKey, validationsByField })
    if (validationResults?.length > 0) {
      acc[fieldKey] = validationResults
    }
    return acc
  }, {})

export const InputFieldValidator = {
  validateField,
  validateFields,
  validateRequired,
  validateLessThan,
  validateGreaterThan,
}
