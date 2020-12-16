import { SHOW_SYSTEM_ERROR } from 'actions/systemError'

const systemError = (
  state = {
    show: false,
    message: null,
    stackTrace: null,
  },
  action
) => {
  switch (action.type) {
    case SHOW_SYSTEM_ERROR:
      const { message, stackTrace } = action
      return { ...state, show: true, message, stackTrace }
    default:
      return state
  }
}

export default systemError
