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
}
    