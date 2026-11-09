import { useLocation } from 'react-router-dom'

export const withLocation = (Component) => {
  const ComponentWithLocation = (props) => {
    const location = useLocation()

    return <Component {...props} location={location} />
  }

  return ComponentWithLocation
}
