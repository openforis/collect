import { useNavigate } from 'react-router-dom'

export const withNavigate = (Component) => {
  const ComponentWithNavigate = (props) => {
    const navigate = useNavigate()

    return <Component {...props} navigate={navigate} />
  }

  return ComponentWithNavigate
}
