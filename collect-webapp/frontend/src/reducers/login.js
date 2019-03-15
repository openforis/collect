import {
    LOG_IN_PENDING, LOG_IN_SUCCESS, LOG_IN_FAILED
    } from 'actions/login'

function login(
    state = {
        isPending: false,
        isSuccess: false,
        error: null
}, action
    ) {
      switch (action.type) {
        case LOG_IN_PENDING:
          return Object.assign({}, state, {
            isPending: true,
            isSuccess: false,
            error: null
          })
        case LOG_IN_SUCCESS:
            return Object.assign({}, state, {
                isPending: false,
                isSuccess: true
            })
        case LOG_IN_FAILED:
            return Object.assign({}, state, {
                isPending: false,
                isSuccess: false,
                error: action.error
            })
        default:
          return state
      }
    }
    
    export default login