import Objects from './Objects'

export default class Arrays {

    static isEmpty(array) {
        return Objects.isNullOrUndefined(array) || array.length === 0
    }

    static isNotEmpty(array) {
        return ! Arrays.isEmpty(array)
    }

    static contains(array, itemOrPredicate, keyProp) {
        if (itemOrPredicate instanceof Function) {
            return array.find(itemOrPredicate) !== undefined
        } else {
            return Arrays.indexOf(array, itemOrPredicate, keyProp) >= 0
        }
    }

    static clone(array) {
        return array.slice(0)
    }

    static uniqueItemOrNull(array) {
		return array.length === 1 ? array[0] : null;
	}

    static indexOf(array, item, keyProp=null) {
        if (keyProp === null) {
            return array.indexOf(item)
        } else {
            const keyValuePairs = {}
            keyValuePairs[keyProp] = item[keyProp]
            const idx = array.findIndex(el => Objects.matchesKeyValuePairs(el, keyValuePairs))
            return idx
        }
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
    static addItem(array, item, onlyIfNotExists=false, keyProp) {
        if (onlyIfNotExists && Arrays.contains(array, item, keyProp)) {
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
    static removeItem(array, item, keyProp) {
        const idx = Arrays.indexOf(array, item, keyProp)
        if (idx < 0) {
            return Arrays.clone(array)
        } else {
            return array.slice(0, idx).concat(array.slice(idx + 1))
        }
    }

    static removeItems(array, items, keyProp) {
        return Arrays.addOrRemoveItems(array, items, true, keyProp)
    }

    /**
     * Adds or removes an element without side effect on the specified array
     * @param {Array} array 
     * @param {Object} item 
     * @param {boolean} remove 
     * 
     * @returns {Array}
     */
    static addOrRemoveItem(array, item, remove=false, keyProp) {
        if (remove) {
            return Arrays.removeItem(array, item, keyProp)
        } else {
            return Arrays.addItem(array, item, true, keyProp)
        }
    }

    static addOrRemoveItems(array, items, remove=false, keyProp) {
        let result = Arrays.clone(array)
        items.forEach(item => result = Arrays.addOrRemoveItem(result, item, remove, keyProp))
        return result
    }

    static sort(array, prop) {
        array.sort((a,b) => 
                a === null && b === null ? 0: 
                a === null ? -1: 
                b === null ? 1: 
                Objects.compare(a[prop], b[prop])
        )
    }

    static singleItemOrNull(items) {
		return items.length === 1 ? items[0] : null;
    }

    static groupBy(array, prop) {
        return array.reduce((itemsByProp, item) => {
            const propValue = item[prop]
            let group = itemsByProp[propValue]
            if (!group) {
                group = []
                itemsByProp[propValue] = group
            }
            group.push(item)
            return itemsByProp
        }, {});
    }

    static replaceItemAt(array, index, newItem) {
        const cloned = Arrays.clone(array)
        cloned[index] = newItem
        return cloned
    }

}