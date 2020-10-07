import { useWindowResize } from 'common/hooks'

const WindowResizeListenerComponent = (props) => {
  const { onResize } = props

  useWindowResize(onResize)

  return null
}

export default WindowResizeListenerComponent
