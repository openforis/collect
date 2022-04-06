import React from 'react'
import MenuItem from '@material-ui/core/MenuItem'
import Select from '@material-ui/core/Select'
import Chip from '@material-ui/core/Chip'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'
import Strings from 'utils/Strings'
import { OutlinedInput } from '@material-ui/core'

/*
const styles = theme => ({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  formControl: {
    margin: theme.spacing.unit,
    minWidth: 120,
    maxWidth: 300,
  },
  chips: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  chip: {
    margin: theme.spacing.unit / 4,
  }
})
*/
class DataGridSelectFilter extends React.Component {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
    this.isFiltered = this.isFiltered.bind(this)
    this.buildMenuItems = this.buildMenuItems.bind(this)
    this.buildFixedMenuItems = this.buildFixedMenuItems.bind(this)

    this.state = {
      selectedValues: [''],
      allValuesSelected: true,
      dataSource: this.extractDataSource(),
    }
  }

  extractDataSource() {
    return this.props.dataSource
  }

  handleChange(e) {
    const { filterHandler } = this.props
    const { dataSource } = this.state

    const val = Arrays.toArray(e.target.value)
    const allValuesPreviouslySelected = this.state.allValuesSelected
    const allValuesSelected =
      val.length === 0 ||
      (Arrays.contains(val, '') && !allValuesPreviouslySelected) ||
      (!Arrays.contains(val, '') && val.length === dataSource.length)
    const selectedValues = allValuesSelected ? [''] : Arrays.removeItem(val, '')

    this.setState({
      allValuesSelected,
      selectedValues,
    })

    if (allValuesSelected) {
      //remove the filter
      filterHandler()
    } else {
      filterHandler(selectedValues)
    }
  }

  isFiltered(_targetValue) {
    return true
  }

  buildFixedMenuItems() {
    const allValuesItem = (
      <MenuItem key="---all---" value="">
        <em>{L.l('global.all.menuitem')}</em>
      </MenuItem>
    )
    return [allValuesItem]
  }

  buildMenuItems() {
    const { dataSource } = this.state

    const fixedMenuItems = this.buildFixedMenuItems()
    const menuItems = [fixedMenuItems].concat(
      dataSource
        .sort((item1, item2) => Strings.compare(item1.label, item2.label))
        .map((item) => (
          <MenuItem key={item.value} value={item.value}>
            {item.label}
          </MenuItem>
        ))
    )
    return menuItems
  }

  isDataSourceItemSelected(_selectedValues) {
    return !this.state.allValuesSelected
  }

  getFixedItemLabel(value) {
    switch (value) {
      case '':
        return L.l('global.all.menuitem')
      default:
        return null
    }
  }

  render() {
    const { multiple } = this.props
    const { dataSource, selectedValues } = this.state

    const menuItems = this.buildMenuItems()
    return (
      <Select
        multiple={multiple}
        value={selectedValues}
        onChange={this.handleChange}
        displayEmpty
        renderValue={(selectedValues) => {
          if (!this.isDataSourceItemSelected(selectedValues)) {
            return (
              <div>
                <em>{this.getFixedItemLabel(selectedValues[0])}</em>
              </div>
            )
          } else {
            return (
              <div>
                {selectedValues.map((value) => {
                  const item = dataSource.find((item) => item.value === value)
                  return <Chip key={value} label={item.label} size="small" />
                })}
              </div>
            )
          }
        }}
      >
        {menuItems}
      </Select>
    )
  }
}

//export default withStyles(styles, { withTheme: true })(SelectFilter)
export default DataGridSelectFilter
