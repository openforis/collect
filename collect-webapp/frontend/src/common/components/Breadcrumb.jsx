import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Breadcrumb, BreadcrumbItem } from 'reactstrap'
import Routes from 'Routes'

const getPaths = (pathname) => {
  const paths = ['/']

  if (pathname === '/') return paths

  pathname.split('/').reduce((prev, curr, index) => {
    const currPath = `${prev}/${curr}`
    paths.push(currPath)
    return currPath
  })
  return paths
}

const BreadcrumbsItem = (props) => {
  const { pathPart } = props
  const { pathname } = useLocation()

  const route = Routes.findRouteByPath(pathPart)
  if (route) {
    return pathname === pathPart ? (
      <BreadcrumbItem active>{route.name}</BreadcrumbItem>
    ) : (
      <BreadcrumbItem>
        <Link to={route.path || ''}>{route.name}</Link>
      </BreadcrumbItem>
    )
  }
  return null
}

export default () => {
  const location = useLocation()
  const paths = getPaths(location.pathname)
  const items = paths.map((pathPart, i) => <BreadcrumbsItem key={String(i)} pathPart={pathPart} />)

  return (
    <div>
      <Breadcrumb>{items}</Breadcrumb>
    </div>
  )
}
