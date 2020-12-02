import React from 'react'
import PropTypes from 'prop-types'

import {
  Button,
  Dialog,
  DialogTitle,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@material-ui/core'
import L from 'utils/Labels'
import Dates from 'utils/Dates'

const NewRecordParametersDialog = (props) => {
  const { onClose, onOk, versions, open } = props

  return (
    <Dialog onClose={onClose} aria-labelledby="new-record-parameters-dialog-title" open={open}>
      <DialogTitle id="new-record-parameters-dialog-title">
        {L.l('dataManagement.formVersioning.selectFormVersion')}
      </DialogTitle>
      <div>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>{L.l('common.name')}</TableCell>
              <TableCell>{L.l('common.label')}</TableCell>
              <TableCell>{L.l('common.date')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {versions.map((v) => (
              <TableRow key={v.id} hover onClick={() => onOk(v.id)}>
                <TableCell>{v.name}</TableCell>
                <TableCell>{v.label}</TableCell>
                <TableCell>{Dates.formatDateIso(v.date)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <DialogActions>
        <Button onClick={onClose}>{L.l('general.cancel')}</Button>
      </DialogActions>
    </Dialog>
  )
}

NewRecordParametersDialog.propTypes = {
  onClose: PropTypes.func.isRequired,
  onOk: PropTypes.func.isRequired,
  open: PropTypes.bool,
  versions: PropTypes.array.isRequired,
}

NewRecordParametersDialog.defaultProps = {
  open: false,
}

export default NewRecordParametersDialog
