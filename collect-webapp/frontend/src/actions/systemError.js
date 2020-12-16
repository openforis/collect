export const SHOW_SYSTEM_ERROR = 'SYSTEM_ERROR'

export const showSystemError = ({ message, stackTrace }) => ({
  type: SHOW_SYSTEM_ERROR,
  message,
  stackTrace,
})
