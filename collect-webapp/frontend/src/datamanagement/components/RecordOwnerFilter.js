import React from 'react'
import MenuItem from '@material-ui/core/MenuItem'
import { connect } from 'react-redux'

import SelectFilter from 'common/components/datatable/SelectFilter'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'

const ONLY_ME = '___ONLY_ME___'

class RecordOwnerFilter extends SelectFilter {
  constructor(props, context) {
    super(props, context)

    this.buildFixedMenuItems = this.buildFixedMenuItems.bind(this)

    this.state = Object.assign(this.state, {
      onlyMeSelected: false,
    })
  }

  buildFixedMenuItems() {
    const fixedMenuItems = super.buildFixedMenuItems()
    const onlyMeMenuItem = (
      <MenuItem key={ONLY_ME} value={ONLY_ME}>
        <em>{L.l('global.onlyme.menuitem')}</em>
      </MenuItem>
    )
    return fixedMenuItems.concat([onlyMeMenuItem])
  }

  getFixedItemLabel(value) {
    switch (value) {
      case ONLY_ME:
        return L.l('global.onlyme.menuitem')
      default:
        return super.getFixedItemLabel(value)
    }
  }

  isDataSourceItemSelected(selectedValues) {
    return super.isDataSourceItemSelected(selectedValues) && !this.state.onlyMeSelected
  }

  handleChange(e) {
    const loggedUserId = this.props.loggedUser.id
    const val = e.target.value
    const notFixedValues = Arrays.removeItems(val, ['', ONLY_ME])

    const onlyMeSelected = Arrays.contains(val, ONLY_ME) && !this.state.onlyMeSelected

    const allValuesSelected =
      val.length === 0 ||
      (Arrays.contains(val, '') && !this.state.allValuesSelected) ||
      (!Arrays.contains(val, '') && notFixedValues.length === this.props.dataSource.length)

    const selectedValues = allValuesSelected ? [''] : onlyMeSelected ? [ONLY_ME] : notFixedValues

    const filterValues = allValuesSelected ? null : onlyMeSelected ? [loggedUserId] : notFixedValues

    this.setState({
      allValuesSelected: allValuesSelected,
      selectedValues: selectedValues,
      onlyMeSelected: onlyMeSelected,
    })

    if (filterValues === null) {
      this.props.filterHandler()
    } else {
      this.props.filterHandler(filterValues)
    }
  }

  render() {
    return super.render()
  }
}

const mapStateToProps = (state) => {
  return {
    loggedUser: state.session ? state.session.loggedUser : null,
  }
}

export default connect(mapStateToProps)(RecordOwnerFilter)
