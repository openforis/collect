import React, { Component } from 'react';

export default class AbstractItemsListPage extends Component {

    constructor( props ) {
		super( props );

        this.state = { 
            selectedItems: [],
            selectedItemIds: [],
            editedItem: null
        };
        
		this.handleRowSelect = this.handleRowSelect.bind(this);
		this.handleNewButtonClick = this.handleNewButtonClick.bind(this);
    }
    
    handleRowSelect(row, isSelected, e) {
		if (isSelected) {
			this.handleItemSelected(row);
		} else {
			this.handleItemUnselected(row);
		}
	}

	handleItemSelected(item) {
		let newSelectedItems = this.state.selectedItems.concat([item]);
		this.handleItemsSelection(newSelectedItems)
	}

	handleItemUnselected(item) {
		let idx = this.state.selectedItems.indexOf(item);
		let newSelectedItems = this.state.selectedItems.slice(idx, 0);
		this.handleItemsSelection(newSelectedItems)
	}

	handleItemsSelection(selectedItems) {
		this.setState({ ...this.state, 
			selectedItems: selectedItems,
			selectedItemIds: selectedItems.map(item => item.id),
			editedItem: this.getUniqueItemOrNull(selectedItems)
		})
	}

	getUniqueItemOrNull(items) {
		return items.length === 1 ? items[0] : null;
	}
	
	handleNewButtonClick() {
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