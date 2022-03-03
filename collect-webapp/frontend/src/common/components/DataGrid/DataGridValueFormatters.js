import Dates from 'utils/Dates'

export const DataGridValueFormatters = {
  date: ({ value }) => (value > 0 ? Dates.format(new Date(value)) : ''),
  dateTime: ({ value }) => (value > 0 ? Dates.formatDatetime(new Date(value)) : ''),
}
