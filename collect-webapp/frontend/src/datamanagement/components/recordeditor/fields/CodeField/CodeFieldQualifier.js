import { TextField } from '@material-ui/core'

import L from 'utils/Labels'

const CodeFieldQualifier = (props) => {
  const { code, qualifier, readOnly, onChangeQualifier } = props

  return (
    <TextField
      value={qualifier}
      variant="outlined"
      placeholder={L.l('common.specify')}
      disabled={readOnly}
      onChange={(event) => onChangeQualifier({ code, qualifier: event.target.value })}
    />
  )
}

export default CodeFieldQualifier
