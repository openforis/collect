import { Component } from 'react';
import L from 'utils/Labels'

export default class AbstractItemDetailsPage extends Component {

    constructor(props) {
        super(props);

        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleDeleteBtnClick = this.handleDeleteBtnClick.bind(this);
        this.updateStateFromResponse = this.updateStateFromResponse.bind(this);

        this.state = this.getInitialState()
    }

    getInitialState() {
        return {
            errorFeedback: [], 
            alertMessageOpen: false, 
            alertMessageColor: 'success', 
            alertMessageText: null
        }
    }

    componentDidMount() {
        this.updateStateFromProps(this.props)
    }

    componentWillReceiveProps(nextProps) {
        this.updateStateFromProps(nextProps);
    }
    
    updateStateFromProps(props) {
    }

    updateStateFromResponse(res) {
        let errorFeedback = {};
        let isError = res.status === 'ERROR';
        if (isError) {
            res.errors.forEach(error => {
                errorFeedback[error.field] = L.l(error.code)
            })
        }
        let alertMessageOpen = true;
        let alertMessageColor = isError ? 'danger' : 'success';
        let alertMessageText = L.l(isError ? 'validation.errorsInTheForm' : 'global.save.success');
        
        this.setState({...this.state, 
            errorFeedback: errorFeedback, 
            alertMessageOpen: alertMessageOpen, 
            alertMessageColor: alertMessageColor, 
            alertMessageText: alertMessageText
        })
    }

    handleSaveBtnClick() {
    }

    handleDeleteBtnClick() {
    }

    getFieldState(field) {
        return this.state.errorFeedback[field] ? 'danger' : ''
    }

}
