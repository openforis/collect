export default class Strings {

    static replaceAll(target, search, replacement) {
        return target.replace(new RegExp(search, 'g'), replacement)
    }

    static trimToEmpty(value) {
        return value === null ? '' : value.toString().trim()
    }

    static isNotBlank(value) {
        return value !== null && value.length > 0
    }

    static compare(a, b) {
        return a > b ? 1 : b > a ? -1 : 0
    }
}
    