import './SystemErrorDialog.scss'

import React from 'react'
import PropTypes from 'prop-types'

import {
  Button,
  DialogTitle,
  DialogContent,
  DialogContentText,
  LinearProgress,
  DialogActions,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@material-ui/core'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'

import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'
import { Dialog } from 'common/components'

const SystemErrorDialog = (props) => {
  const { details, message, showProgressBar, showRefreshButton, title, width } = props

  return (
    <Dialog open disableBackdropClick disableEscapeKeyDown maxWidth={false} className="system-error-dialog">
      <DialogTitle>{L.l(title)}</DialogTitle>
      <DialogContent style={{ width: `${width}px` }}>
        <DialogContentText>
          {L.l('systemError.reportToOpenForisTeam')}:
          <br />
          {message}
        </DialogContentText>
        {showProgressBar && <LinearProgress />}
        {details && (
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>{L.l('systemError.details')}</AccordionSummary>
            <AccordionDetails>
              <div className="details-container">{details}</div>
            </AccordionDetails>
          </Accordion>
        )}
      </DialogContent>
      <DialogActions>
        {showRefreshButton && (
          <Button color="primary" variant="contained" onClick={() => RouterUtils.reloadPage()}>
            {L.l('systemError.reloadPage')}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  )
}

SystemErrorDialog.propTypes = {
  details: PropTypes.string,
  message: PropTypes.string,
  showProgressBar: PropTypes.bool,
  showRefreshButton: PropTypes.bool,
  title: PropTypes.string,
  width: PropTypes.number,
}

SystemErrorDialog.defaultProps = {
  details: null,
  message: null,
  showProgressBar: false,
  showRefreshButton: true,
  title: 'systemError.title',
  width: 400,
}

export default SystemErrorDialog
