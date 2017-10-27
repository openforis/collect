export default class Objects {

    static compare(a, b) {
        if (a === null && b === null) {
            return 0
        } else if (a === null) {
            return -1
        } else if (b === null) {
            return 1
        } else {
            if (typeof a === 'string' && typeof b === 'string') {
                return a.localeCompare(b)
            } else {
                return a === b ? 0 : a > b ? 1 : -1
            }
        }
    }

    static isNullOrUndefined(obj) {
        return obj === null || typeof obj === 'undefined'
    }

    static isNotNullOrUndefined(obj) {
        return ! Objects.isNullOrUndefined(obj)
    }

    static defaultIfNull(obj, defaultVal) {
        if (Objects.isNullOrUndefined(obj)) {
            return defaultVal
        } else {
            return obj
        }
    }

}