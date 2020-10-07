import { useWindowResize } from 'common/hooks'
import { useCallback } from 'react'
import { useEffect } from 'react'
import Containers from './Containers'

const TableResizeOnWindowResizeComponent = (props) => {
  const { wrapperRef, margin } = props

  const resizeTable = useCallback(() => {
    const wrapper = wrapperRef.current
    if (wrapper) {
      Containers.extendTableHeightToMaxAvailable(wrapper, margin)
    }
  }, [wrapperRef, margin])

  useWindowResize(resizeTable)

  useEffect(() => {
    resizeTable()
  }, [wrapperRef, resizeTable])

  return null
}

TableResizeOnWindowResizeComponent.defaultProps = {
  margin: 122,
}

export default TableResizeOnWindowResizeComponent
