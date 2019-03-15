import { Component } from 'react';
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
		this.handleAllRowsSelect = this.handleAllRowsSelect.bind(this)
		this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
		this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
    }
    
    handleRowSelect(row, isSelected, e) {
		let newSelectedItems
		if (isSelected) {
			newSelectedItems = this.state.singleSelection ? [row] : Arrays.addItem(this.state.selectedItems, row);
		} else {
			newSelectedItems = Arrays.removeItem(this.state.selectedItems, row)
		}
		this.handleItemsSelection(newSelectedItems)
	}

	handleAllRowsSelect(isSelected, rows) {
		let newSelectedItems = Arrays.addOrRemoveItems(this.state.selectedItems, rows, !isSelected);
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