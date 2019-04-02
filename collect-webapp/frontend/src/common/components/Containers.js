import ReactDOM from 'react-dom';

export default class Containers {

    static extendToMaxAvailableHeight(parentContainer, componentClassName, margin) {
        if (!margin) {
            margin = 0
        }
        const domParentContainer = ReactDOM.findDOMNode(parentContainer)
        const domComponent = domParentContainer.getElementsByClassName(componentClassName)[0]
        domComponent.style.height = (domParentContainer.clientHeight - margin) + 'px'
    }

    static extendTableHeightToMaxAvailable(parentContainer, margin) {
        if (!margin) {
            margin = 0
        }
        const domParentContainer = ReactDOM.findDOMNode(parentContainer)
        const tableContainer = domParentContainer.getElementsByClassName('react-bs-table-container')[0]
        const tableHeader = domParentContainer.getElementsByClassName('react-bs-container-header')[0]
        const tableBody = domParentContainer.getElementsByClassName('react-bs-container-body')[0]
        tableContainer.style.height = (domParentContainer.clientHeight - margin) + 'px'
        tableBody.style.height = (tableContainer.clientHeight - tableHeader.clientHeight) + 'px'
    }

}