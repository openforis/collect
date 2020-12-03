const { default: AbstractField } = require('./AbstractField')

export default class AbstractNumericField extends AbstractField {
  constructor(props) {
    super(props)

    this.onUnitChange = this.onUnitChange.bind(this)
  }

  getSelectedUnitIdFromProps() {
    const attr = this.getAttribute()
    const { unitId } = attr
    if (unitId) {
      return unitId
    }
    const { definition } = attr
    const { precisions } = definition
    if (precisions.length) {
      const defaultPrecision = precisions.find((precision) => precision.defaultPrecision) || precisions[0]
      return defaultPrecision.unitId
    }
    return null
  }

  onUnitChange(event) {
    const { value } = this.state
    const valueUpdated = { ...value, unit: Number(event.target.value) }
    this.updateValue({ value: valueUpdated })
  }
}
