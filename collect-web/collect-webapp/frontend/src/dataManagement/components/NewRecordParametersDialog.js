import React, { Component } from 'react'
import PropTypes from 'prop-types'

import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogTitle from '@material-ui/core/DialogTitle'
import DialogActions from '@material-ui/core/DialogActions'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import L from 'utils/Labels'
import Dates from 'utils/Dates'

export default class NewRecordParametersDialog extends Component {

    constructor(props) {
        super(props)

        this.handleVersionClick = this.handleVersionClick.bind(this)
        this.handleClose = this.handleClose.bind(this)
    }

    handleVersionClick = versionName => {
        this.props.onOk(versionName)
    }

    handleClose = () => {
        this.props.onClose()
    }

    render() {
        const { onClose, versions, onOk, ...other } = this.props

        return (
            <Dialog onClose={this.handleClose} aria-labelledby="new-record-parameters-dialog-title" {...other}>
                <DialogTitle id="new-record-parameters-dialog-title">
                    {L.l('dataManagement.formVersioning.selectFormVersion')}
                </DialogTitle>
                <div>
                    <Table>
                        <TableBody>
                            {versions.map(v => {
                                return (
                                <TableRow key={v.id} hover onClick={event => this.handleVersionClick(v.name)}>
                                    <TableCell>{v.name}</TableCell>
                                    <TableCell>{v.label}</TableCell>
                                    <TableCell>{Dates.format(v.date)}</TableCell>
                                </TableRow>
                                )
                            })}
                        </TableBody>
                    </Table>
                </div>
                <DialogActions>
                    <Button onClick={this.handleClose}>
                        {L.l('general.cancel')}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }
}

NewRecordParametersDialog.propTypes = {
    versions: PropTypes.array.isRequired,
    onClose: PropTypes.func.isRequired,
    onOk: PropTypes.func.isRequired
}
