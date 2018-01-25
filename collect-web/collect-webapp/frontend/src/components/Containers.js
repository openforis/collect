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

}