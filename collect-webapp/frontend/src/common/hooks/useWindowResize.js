import { useEffect } from 'react'

export const useWindowResize = (onResize) => {
  useEffect(() => {
    window.addEventListener('resize', onResize)

    return () => window.removeEventListener('resize', onResize)
  }, [onResize])
}
