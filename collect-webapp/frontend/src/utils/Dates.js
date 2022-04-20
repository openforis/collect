import DateFnsUtils from '@date-io/date-fns'

const dateFnsUtils = new DateFnsUtils()

export default class Dates {
  static DATE_FORMAT = 'dd/MM/yyyy'
  static DATE_FORMAT_ISO = 'yyyy-MM-dd'
  static TIME_FORMAT = 'HH:mm'
  static DATETIME_FORMAT = `${Dates.DATE_FORMAT} ${Dates.TIME_FORMAT}`
  static MILLIS = 'millis'
  static DAYS = 'days'
  static MONTHS = 'months'
  static YEARS = 'years'

  static format(date, pattern = Dates.DATE_FORMAT) {
    return date == null ? '' : dateFnsUtils.format(date, pattern)
  }

  static formatDatetime(dateTime) {
    return Dates.format(dateTime, Dates.DATETIME_FORMAT)
  }

  static parseDateISO(dateStr) {
    return dateStr ? dateFnsUtils.parse(dateStr, Dates.DATE_FORMAT_ISO) : null
  }

  static formatDateIso(dateStr) {
    const date = Dates.parseDateISO(dateStr)
    return date ? Dates.format(date) : null
  }

  static compare(date1, date2, datePart) {
    if (!datePart) {
      datePart = Dates.MILLIS
    }
    const d1 = new Date(date1.getTime())
    const d2 = new Date(date2.getTime())

    if (datePart === Dates.YEARS) {
      d1.setMonth(0)
      d2.setMonth(0)
    }
    if (datePart === Dates.MONTHS) {
      d1.setDate(1)
      d2.setDate(1)
    }
    d1.setHours(0, 0, 0, 0)
    d2.setHours(0, 0, 0, 0)

    return d1.getTime() < d2.getTime() ? -1 : d1.getTime() > d2.getTime() ? 1 : 0
  }

  static getYear(date) {
    return dateFnsUtils.getYear(date)
  }

  static getMonth(date) {
    return dateFnsUtils.getMonth(date)
  }

  static getDay(date) {
    return Number(dateFnsUtils.getDayText(date))
  }

  static getHours(date) {
    return dateFnsUtils.getHours(date)
  }

  static getMinutes(date) {
    return dateFnsUtils.getMinutes(date)
  }
}
