import React, { Component } from 'react'
import { MenuItem } from 'material-ui/Menu'
import { withStyles } from 'material-ui/styles'
import Select from 'material-ui/Select'
import Chip from 'material-ui/Chip'

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
      selectedValues: []
    }
  }

  handleChange(e) {
    const selectedValues = e.target.value
    this.setState({ selectedValues: selectedValues })
    if (selectedValues.length == 0) {
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
    const { selectedValues } = this.state

    const menuItems = dataSource.map(item => {
      return <MenuItem 
              key={item.value} 
              value={item.value}>
              {item.label}
          </MenuItem>
    })
    return (
      <Select
        multiple={multiple}
        value={selectedValues}
        onChange={this.handleChange}
        renderValue={selected => (
            <div className={classes.chips}>
              {selected.map(value => {
                const item = dataSource.find(item => item.value === value)
                return <Chip key={value} label={item.label} className={classes.chip} />
              })}
            </div>
        )}>
        {menuItems}
      </Select>
    )
  }
}

export default withStyles(styles, { withTheme: true })(SelectFilter);