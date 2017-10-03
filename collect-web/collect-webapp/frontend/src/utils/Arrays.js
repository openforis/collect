
export default class Arrays {

    /**
     * Adds an element without side effect on the specified array
     * 
     * @param {Array} array 
     * @param {Object} item 
     */
    static addItem(array, item) {
        return array.concat([item])
    }

    /**
     * Removes an element without side effect on the specified array
     * 
     * @param {Array} array 
     * @param {Object} item 
     */
    static removeItem(array, item) {
        const idx = array.indexOf(item)
        return array.slice(0, idx).concat(array.slice(idx + 1))
    }

    /**
     * Adds or removes an element without side effect on the specified array
     * @param {Array} array 
     * @param {Object} item 
     * @param {boolean} remove 
     */
    static addOrRemoveItem(array, item, remove=false) {
        if (remove) {
            return Arrays.removeItem(array, item)
        } else {
            return Arrays.addItem(array, item)
        }
    }

}