import ServiceFactory from 'services/ServiceFactory'

export const REQUEST_APPLICATION_INFO = 'REQUEST_APPLICATION_INFO'
export const RECEIVE_APPLICATION_INFO = 'RECEIVE_APPLICATION_INFO'

export const RECORD_DELETED = 'RECORD_DELETED'
export const RECORDS_DELETED = 'RECORDS_DELETED'

//APPLICATION INFO
const requestApplicationInfo = () => ({
  type: REQUEST_APPLICATION_INFO,
})

const receiveApplicationInfo = (info) => ({
  type: RECEIVE_APPLICATION_INFO,
  info,
})

export const fetchApplicationInfo = () => (dispatch) => {
  dispatch(requestApplicationInfo())
  ServiceFactory.applicationInfoService.fetchInfo().then((info) => {
    dispatch(receiveApplicationInfo(info))
  })
}
