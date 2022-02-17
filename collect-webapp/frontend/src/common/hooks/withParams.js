import { useParams } from 'react-router-dom'

export const withParams = (Component) => {
  const ComponentWithParams = (props) => {
    const params = useParams()

    return <Component {...props} params={params} />
  }

  return ComponentWithParams
}
