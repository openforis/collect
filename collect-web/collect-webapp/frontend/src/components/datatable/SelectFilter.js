import React, { Component } from 'react'
import { MenuItem } from 'material-ui/Menu'
import { withStyles } from 'material-ui/styles'
import Select from 'material-ui/Select'
import Chip from 'material-ui/Chip'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

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

class SelectFilter extends React.Component {
  constructor(props) {
    super(props)
    this.handleChange = this.handleChange.bind(this)
    this.isFiltered = this.isFiltered.bind(this)

    this.state = {
      selectedValues: [''],
      allValuesSelected: true
    }
  }

  handleChange(e) {
    const val = e.target.value
    const allValuesPreviouslySelected = this.state.allValuesSelected
    const allValuesSelected = val.length === 0 
      || Arrays.contains(val, '') && !allValuesPreviouslySelected 
      || !Arrays.contains(val, '') && val.length === this.props.dataSource.length
    const selectedValues = allValuesSelected ? [''] : Arrays.removeItem(val, '')
    
    this.setState({ 
      allValuesSelected: allValuesSelected,
      selectedValues: selectedValues 
    })

    if (selectedValues.length == 0 || allValuesSelected) {
      //remove the filter
      this.props.filterHandler()
    } else {
      this.props.filterHandler(selectedValues)
    }
  }

  isFiltered(targetValue) {
    return true
  }

  render() {
    const { classes, theme, multiple, dataSource } = this.props
    const { selectedValues, allValuesSelected } = this.state

    const allValuesItem = <MenuItem key="---all---" value=""><em>{L.l('global.all.menuitem')}</em></MenuItem>
    const menuItems = [allValuesItem].concat(dataSource
            .sort((item1, item2) => Strings.compare(item1.label, item2.label))
            .map(item =>
              <MenuItem 
                  key={item.value} 
                  value={item.value}>
                  {item.label}
              </MenuItem>
    ))
    return (
      <Select
        multiple={multiple}
        value={selectedValues}
        onChange={this.handleChange}
        displayEmpty
        renderValue={selected => {
            console.log("selectedValues")
            console.log(selectedValues)
            console.log("selected")
            console.log(selected)
            console.log("allValuesSelected")
            console.log(allValuesSelected)
            if (allValuesSelected) {
              return <div><em>{L.l('global.all.menuitem')}</em></div>
            } else {
              return <div className={classes.chips}>
                  {selected.map(value => {
                    const item = dataSource.find(item => item.value === value)
                    return <Chip key={value} label={item.label} className={classes.chip} />
                  })}
                </div>
            }
        }}
        >
        {menuItems}
      </Select>
    )
  }
}

export default withStyles(styles, { withTheme: true })(SelectFilter);