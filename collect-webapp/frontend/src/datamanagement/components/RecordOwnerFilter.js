import React from 'react'
import MenuItem from '@material-ui/core/MenuItem'
import { connect } from 'react-redux'

import { DataGridSelectFilter } from 'common/components'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'

const ONLY_ME = '___ONLY_ME___'

const usersToDataSource = (users) => (users || []).map((user) => ({ value: user.id, label: user.username }))

class RecordOwnerFilter extends DataGridSelectFilter {
  constructor(props, context) {
    super(props, context)

    this.buildFixedMenuItems = this.buildFixedMenuItems.bind(this)

    this.state = Object.assign(this.state, {
      onlyMeSelected: false,
    })
  }

  componentDidUpdate(prevProps) {
    if (this.props.users?.length !== prevProps.users?.length) {
      this.setState({
        dataSource: this.extractDataSource(),
      })
    }
  }

  extractDataSource() {
    return usersToDataSource(this.props.users)
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
    e.stopPropagation()
    const { loggedUser, filterHandler } = this.props
    const { dataSource } = this.state

    const val = Arrays.toArray(e.target.value)
    const notFixedValues = Arrays.removeItems(val, ['', ONLY_ME])

    const onlyMeSelected = Arrays.contains(val, ONLY_ME) && !this.state.onlyMeSelected

    const allValuesSelected =
      val.length === 0 ||
      (Arrays.contains(val, '') && !this.state.allValuesSelected) ||
      (!Arrays.contains(val, '') && notFixedValues.length === dataSource.length)

    const selectedValues = allValuesSelected ? [''] : onlyMeSelected ? [ONLY_ME] : notFixedValues

    const filterValues = allValuesSelected ? null : onlyMeSelected ? [loggedUser.id] : notFixedValues

    this.setState({
      allValuesSelected: allValuesSelected,
      selectedValues: selectedValues,
      onlyMeSelected: onlyMeSelected,
    })

    if (filterValues === null) {
      filterHandler()
    } else {
      filterHandler(filterValues)
    }
  }
}

const mapStateToProps = (state) => {
  return {
    loggedUser: state.session ? state.session.loggedUser : null,
  }
}

export default connect(mapStateToProps)(RecordOwnerFilter)
