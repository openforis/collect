import Objects from './Objects'

export default class Arrays {

    static contains(array, item) {
        return array.indexOf(item) >= 0
    }

    static clone(array) {
        return array.slice(0)
    }

    /**
     * Adds an element without side effect on the specified array
     * 
     * @param {Array} array 
     * @param {Object} item
     * @param {boolean} onlyIfNotExists 
     * 
     * @returns {Array}
     */
    static addItem(array, item, onlyIfNotExists=false) {
        if (onlyIfNotExists && Arrays.contains(array, item)) {
            return Arrays.clone(array)
        } else {
            return array.concat([item])
        }
    }

    /**
     * Removes an element without side effect on the specified array
     * 
     * @param {Array} array 
     * @param {Object} item 
     * 
     * @returns {Array}
     */
    static removeItem(array, item) {
        const idx = array.indexOf(item)
        if (idx < 0) {
            return Arrays.clone(array)
        } else {
            return array.slice(0, idx).concat(array.slice(idx + 1))
        }
    }

    static removeItems(array, items) {
        return Arrays.addOrRemoveItems(array, items, true)
    }

    /**
     * Adds or removes an element without side effect on the specified array
     * @param {Array} array 
     * @param {Object} item 
     * @param {boolean} remove 
     * 
     * @returns {Array}
     */
    static addOrRemoveItem(array, item, remove=false) {
        if (remove) {
            return Arrays.removeItem(array, item)
        } else {
            return Arrays.addItem(array, item)
        }
    }

    static addOrRemoveItems(array, items, remove=false) {
        let result = Arrays.clone(array)
        items.forEach(item => result = Arrays.addOrRemoveItem(result, item, remove))
        return result
    }

    static sort(array, propName) {
        array.sort((a,b) => a === null && b === null ? 0: a === null ? -1: b === null ? 1: 
            Objects.compare(a[propName], b[propName]))
    }

    static singleItemOrNull(items) {
		return items.length === 1 ? items[0] : null;
	}

}