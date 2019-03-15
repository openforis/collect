import { Component } from 'react'
import L from 'utils/Labels'

export default class AbstractItemDetailsPage extends Component {

    constructor(props) {
        super(props)

        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this)
        this.handleDeleteBtnClick = this.handleDeleteBtnClick.bind(this)
        this.handleValidateResponse = this.handleValidateResponse.bind(this)
        this.handleSaveResponse = this.handleSaveResponse.bind(this)
        this.updateStateFromResponse = this.updateStateFromResponse.bind(this)
        this.validateForm = this.validateForm.bind(this)

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
        this.updateStateFromProps(nextProps)
    }
    
    updateStateFromProps(props) {
    }

    handleSaveResponse(res) {
        this.updateStateFromResponse(res)
    }

    updateStateFromResponse(res) {
        this.handleValidateResponse(res)

        const isError = res.status === 'ERROR'

        const alertMessageOpen = true
        const alertMessageColor = isError ? 'danger' : 'success'
        const alertMessageText = L.l(isError ? 'validation.errorsInTheForm' : 'global.save.success')
        
        this.setState({...this.state, 
            alertMessageOpen: alertMessageOpen, 
            alertMessageColor: alertMessageColor, 
            alertMessageText: alertMessageText
        })
    }

    validateForm() {
    }

    handleValidateResponse(res) {
        const isError = res.status === 'ERROR'
        const alertMessageOpen = isError
        const alertMessageColor = isError ? 'danger' : 'success'
        const alertMessageText = isError ? L.l('validation.errorsInTheForm') : ''
        
        const errorFeedback = {}
        if (isError) {
            res.errors.forEach(error => {
                errorFeedback[error.field] = L.l(error.code)
            })
        }
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
