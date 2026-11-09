import React, { Suspense, useState } from 'react'
import WarningIcon from '@mui/icons-material/Warning'

import LoadingSpinnerSmall from './LoadingSpinnerSmall'
import L from 'utils/Labels'

const ImageComponent = (props) => {
  const { src, onClick, maxWidth, maxHeight } = props

  const maxWidthPx = maxWidth ? `${maxWidth}px` : null
  const maxHeightPx = maxHeight ? `${maxHeight}px` : null

  const [error, setError] = useState(null)

  if (error) {
    return (
      <span className="error" title={L.l('dataManagement.dataEntry.attribute.file.errorDownloadingImage')}>
        <WarningIcon />
      </span>
    )
  }
  return (
    <img
      src={src}
      alt="img"
      onClick={onClick}
      style={{ cursor: onClick ? 'pointer' : null, maxWidth: maxWidthPx, maxHeight: maxHeightPx }}
      onError={() => setError(true)}
    />
  )
}

const Image = (props) => {
  const { src, onClick, maxWidth, maxHeight } = props
  return (
    <Suspense fallback={<LoadingSpinnerSmall />}>
      <ImageComponent src={src} onClick={onClick} maxWidth={maxWidth} maxHeight={maxHeight} />
    </Suspense>
  )
}

export default Image
