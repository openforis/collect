import React from 'react'
import MenuItem from '@material-ui/core/MenuItem'
import { connect } from 'react-redux'

import SelectFilter from 'components/datatable/SelectFilter'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'

class RecordOwnerFilter extends SelectFilter {
    
    constructor(props, context) {
        super(props, context)

        this.buildFixedMenuItems = this.buildFixedMenuItems.bind(this)

        this.state = Object.assign(this.state, {
            onlyMeSelected: false
        })
    }

    buildFixedMenuItems() {
        const fixedMenuItems = super.buildFixedMenuItems()
        const onlyMeMenuItem = 
            <MenuItem key="___ONLY_ME___" value="___ONLY_ME___">
                <em>{L.l('global.onlyme.menuitem')}</em>
            </MenuItem>
        return fixedMenuItems.concat([onlyMeMenuItem])
    }

    getFixedItemLabel(value) {
        switch(value) {
            case '___ONLY_ME___':
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
        const notFixedValues = Arrays.removeItems(val, ['', '___ONLY_ME___'])
        
        const onlyMeSelected = Arrays.contains(val, '___ONLY_ME___') && !this.state.onlyMeSelected

        const allValuesSelected = val.length === 0 
          || (Arrays.contains(val, '') && !this.state.allValuesSelected) 
          || (!Arrays.contains(val, '') && notFixedValues.length === this.props.dataSource.length)
        
        const selectedValues = 
            allValuesSelected ? [''] 
            : onlyMeSelected ? ['___ONLY_ME___']
            : notFixedValues

        const filterValues = 
            allValuesSelected ? null 
            : onlyMeSelected ? [loggedUserId] 
            : notFixedValues

        this.setState({ 
          allValuesSelected: allValuesSelected,
          selectedValues: selectedValues,
          onlyMeSelected: onlyMeSelected
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

const mapStateToProps = state => {
	return {
		loggedUser: state.session ? state.session.loggedUser : null
	}
}

export default connect(mapStateToProps)(RecordOwnerFilter)