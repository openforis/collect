class Validation {
  errors = []
  warnings = []

  constructor({ errors = [], warnings = [] } = {}) {
    this.errors = errors
    this.warnings = warnings
  }

  get errorMessage() {
    return this.errors.join('; ')
  }

  get warningMessage() {
    return this.warnings.join('; ')
  }

  addError(error) {
    this.errors.push(error)
  }

  addWarning(warning) {
    this.warnings.push(warning)
  }

  hasErrors() {
    return this.errors && this.errors.length > 0
  }

  hasWarnings() {
    return this.warnings && this.warnings.length > 0
  }

  isEmpty() {
    return !this.hasErrors() && !this.hasWarnings()
  }
}

export default Validation
