export default class Strings {

    static replaceAll(target, search, replacement) {
        return target.replace(new RegExp(search, 'g'), replacement)
    }
}
    