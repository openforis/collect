export default class Serializable {
    fillFromJSON(jsonObj) {
        for (var propName in jsonObj) {
            let newVal = jsonObj[propName]
            let oldVal = this[propName]
            if ((oldVal === null || oldVal === undefined) && newVal != null) {
                this[propName] = newVal
            }
        }
    }
    
    static createArrayFromJSON(jsonArr, itemClassName: any) {
        let result = [];
        for (var i = 0; i < jsonArr.length; i++) {
            var itemJsonObj = jsonArr[i];
            var item = new itemClassName();
            item.fillFromJSON(itemJsonObj);
            result.push(item);
        }
        return result;
    }
}