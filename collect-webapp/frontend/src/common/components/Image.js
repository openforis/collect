import React, { Suspense } from 'react'
import { useImage } from 'react-image'
import { Spinner } from 'reactstrap'

const LoadingSpinner = () => <Spinner color="primary" />

const ImageComponent = (props) => {
  const { src: srcList, onClick, maxWidth, maxHeight } = props
  const { src, isLoading } = useImage({ srcList })

  const maxWidthPx = maxWidth ? `${maxWidth}px` : null
  const maxHeightPx = maxHeight ? `${maxHeight}px` : null

  return isLoading ? (
    <LoadingSpinner />
  ) : (
    <img
      src={src}
      alt="img"
      onClick={onClick}
      style={{ cursor: onClick ? 'pointer' : null, maxWidth: maxWidthPx, maxHeight: maxHeightPx }}
    />
  )
}

const Image = (props) => {
  const { src, onClick, maxWidth, maxHeight } = props
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <ImageComponent src={src} onClick={onClick} maxWidth={maxWidth} maxHeight={maxHeight} />
    </Suspense>
  )
}

export default Image
