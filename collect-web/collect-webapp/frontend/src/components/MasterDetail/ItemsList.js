import React, { Component } from 'react';


class ItemsList extends Component {

    constructor( props ) {
		super( props );

        this.state = { 
            selectedItems: [],
            selectedItemIds: [],
            editedItem: null
        };
        
		this.handleRowSelect = this.handleRowSelect.bind(this);
		this.handleNewClick = this.handleNewClick.bind(this);
    }
    
    handleRowSelect(row, isSelected, e) {
		if (isSelected) {
			this.setState(this.addSelectedItemToState(row));
		} else {
			this.setState(this.removeSelectedItemFromState(row));
		}
	}

	addSelectedItemToState(user) {
		let newSelectedItems = this.state.selectedItems.concat([user]);
		let newSelectedItemIds = this.state.selectedItemIds.concat([user.id]);
		return { ...this.state, 
			selectedItems: newSelectedItems,
			selectedItemIds: newSelectedItemIds,
			editedItem: this.getUniqueItemOrNull(newSelectedItems)
		}
	}

	removeSelectedItemFromState(user) {
		let idx = this.state.selectedItems.indexOf(user);
		let newSelectedItems = this.state.selectedItems.slice(idx, 0);
		let newSelectedItemIds = this.state.selectedItemIds.slice(idx, 0);
		return { ...this.state, 
			selectedItems: newSelectedItems,
			selectedItemIds: newSelectedItemIds,
			editedItem: this.getUniqueItemOrNull(newSelectedItems)
		}
	}

	getUniqueItemOrNull(items) {
		return items.length === 1 ? items[0] : null;
	}
	
	handleNewClick() {
		this.setState({...this.state, 
			editedItem: this.createNewItem(),
			selectedItems: [],
			selectedItemIds: []
		})
    }
    
    createNewItem() {
        //abstract
        return {};
    }
}

export default ItemsList;