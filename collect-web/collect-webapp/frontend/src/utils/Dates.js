import Moment from 'moment';

export default class Dates {

    static MILLIS = 'millis'
    static DAYS = 'days'
    static MONTHS = 'months'
    static YEARS = 'years'

    static format(date) {
        return date == null ? '' : Moment(date).format('DD/MM/YYYY')
    }

    static compare(date1, date2, datePart) {
        if (! datePart) {
            datePart = Dates.MILLIS
        }
        const d1 = new Date(date1.getTime())
        const d2 = new Date(date2.getTime())
        switch(datePart) {
            case Dates.YEARS:
                d1.setMonth(0)
                d2.setMonth(0)
            case Dates.MONTHS:
                d1.setDate(1)
                d2.setDate(1)
            case Dates.DAYS:
                d1.setHours(0, 0, 0, 0)
                d2.setHours(0, 0, 0, 0)
        }
        return d1.getTime() < d2.getTime() ? -1 : d1.getTime() > d2.getTime() ? 1 : 0
    }
}
    