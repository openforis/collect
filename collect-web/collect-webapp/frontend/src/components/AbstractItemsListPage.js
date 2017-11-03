import React, { Component } from 'react';
import Arrays from 'utils/Arrays'

export default class AbstractItemsListPage extends Component {

    constructor( props ) {
		super( props );

        this.state = { 
            selectedItems: [],
			selectedItemIds: [],
			singleSelection: props.singleSelection,
            editedItem: null
        };
        
		this.handleRowSelect = this.handleRowSelect.bind(this)
		this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
		this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
    }
    
    handleRowSelect(row, isSelected, e) {
		if (isSelected) {
			this.handleItemSelected(row);
		} else {
			this.handleItemUnselected(row);
		}
	}

	handleItemSelected(item) {
		const newSelectedItems = this.state.singleSelection ? [item] : Arrays.addItem(this.state.selectedItems, item);
		this.handleItemsSelection(newSelectedItems)
	}

	handleItemUnselected(item) {
		const newSelectedItems = Arrays.removeItem(this.state.selectedItems, item)
		this.handleItemsSelection(newSelectedItems)
	}

	handleItemsSelection(selectedItems) {
		this.setState({ ...this.state, 
			selectedItems: selectedItems,
			selectedItemIds: selectedItems.map(item => item.id),
			editedItem: Arrays.singleItemOrNull(selectedItems)
		})
	}

	handleNewButtonClick() {
		this.setState({...this.state, 
			editedItem: this.createNewItem(),
			selectedItems: [],
			selectedItemIds: []
		})
    }
	
	handleDeleteButtonClick() {
		
	}
	
    createNewItem() {
        //abstract
        return {};
    }
}