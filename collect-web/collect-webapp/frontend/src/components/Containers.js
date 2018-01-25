import ReactDOM from 'react-dom';

export default class Containers {

    static extendToMaxAvailableHeight(parentContainer, componentClassName, topMargin) {
        const domParentContainer = ReactDOM.findDOMNode(parentContainer)
        const domComponent = domParentContainer.getElementsByClassName(componentClassName)[0]
        if (!topMargin) {
            topMargin = 0
        }
        domComponent.style.height = (domParentContainer.clientHeight - topMargin) + 'px'
    }

    static extendTableHeightToMaxAvailable(parentContainer, marginTop) {
        const domParentContainer = ReactDOM.findDOMNode(parentContainer)
        const tableContainer = domParentContainer.getElementsByClassName('react-bs-table-container')[0]
        const tableHeader = domParentContainer.getElementsByClassName('react-bs-container-header')[0]
        const tableBody = domParentContainer.getElementsByClassName('react-bs-container-body')[0]
        tableContainer.style.height = (domParentContainer.clientHeight - marginTop) + 'px'
        tableBody.style.height = (tableContainer.clientHeight - tableHeader.clientHeight) + 'px'
    }

}