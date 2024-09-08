import { useSelector } from 'react-redux'

export const SessionSelectors = {
  useLoggedInUser: () => useSelector((state) => state?.session?.loggedUser),
}
