import React, { Component } from 'react';


class ItemDetails extends Component {

    constructor(props) {
        super(props);

        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleDeleteBtnClick = this.handleDeleteBtnClick.bind(this);
        this.updateStateFromResponse = this.updateStateFromResponse.bind(this);

        this.updateStateFromProps(props);
    }

    getFieldState(field) {
        return this.state.errorFeedback[field] ? 'danger' : ''
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
                errorFeedback[error.field] = error.code; //TODO give i18n label
            })
        }
        let alertMessageOpen = true;
        let alertMessageColor = isError ? 'danger' : 'success';
        let alertMessageText = isError ? 'user.save.errors_in_form' : 'user.save.success';
        
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
}

export default ItemDetails;