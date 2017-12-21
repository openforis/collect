import Constants from 'utils/Constants';
import Objects from 'utils/Objects';
import Strings from 'utils/Strings';

export default class AbstractService {

    BASE_URL = Constants.API_BASE_URL;
    
    constructor() {
        this._handleError = this._handleError.bind(this)
    }

    get(url, data) {
        return this._getDeleteOrPatch('GET', url, data)
    }

    delete(url, data) {
        return this._getDeleteOrPatch('DELETE', url, data)
    }

    post(url, data) {
        let body = this._toQueryData(data);

        return fetch(this.BASE_URL + url, {
            credentials: 'include',
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: body
        }).then(response => response.json(),
            this._handleError)
        .catch(error => {
            throw(error);
        })
    }

    postJson(url, data) {
        return this._sendJson(url, data, 'POST')
    }

    postFormData(url, data) {
        const formData = new FormData()
        for(var name in data) {
            const value = data[name]
            if (Objects.isNotNullOrUndefined(value)) {
                formData.append(name, value)
            }
        }

        return fetch(this.BASE_URL + url, {
            credentials: 'include',
            method: 'POST',
            body: formData
        }).then(response => response.json(),
            this._handleError)
        .catch(error => {
            throw(error);
        })
    }

    patch(url, data) {
        return this._getDeleteOrPatch('PATCH', url, data)
    }

    patchJson(url, data) {
        return this._sendJson(url, data, 'PATCH')
    }

    _getDeleteOrPatch(method, url, data) {
        let queryData = this._toQueryData(data, null, false);
        return fetch(this.BASE_URL + url + (queryData === null ? '': '?' + queryData), {
                credentials: 'include',
                method: method
            })
            .then(response => {
                if (response.ok) {
                    return response.json()
                } else {
                    throw Error(response.statusText)
                }
            }, this._handleError)
            .catch(error => {
                throw(error);
            })
    }

    _sendJson(url, data, method) {
        return fetch(this.BASE_URL + url, {
            credentials: 'include',
            method: method,
            headers: {
              'Content-Type': 'application/json'
            },
            body: data ? JSON.stringify(data) : null
        }).then(response => response.json(),
            this._handleError)
        .catch(error => {
            throw(error);
        })
    }

    downloadFile(url, windowName) {
        window.open(url, windowName ? windowName : '_blank')
    }

    _toQueryData(data, propPrefix, includeNulls = true) {
        if (! data) {
            return null
        }
        return Object.keys(data).map((key) => {
            const val = data[key];
            if (val === null || val === '') {
                if (includeNulls) {
                    return (propPrefix ? propPrefix : '') + encodeURIComponent(key) + '=' + encodeURIComponent(Strings.trimToEmpty(val))
                } else {
                    return null
                }
            } else {
                if (val instanceof Array) {
                    const arr = val
                    const arrQueryDataParts = []
                    for(let i=0; i<arr.length; i++) {
                        const itemVal = arr[i]
                        if (itemVal !== null && itemVal !== undefined) {
                            if (typeof itemVal === 'object') {
                                const nestedPropPrefix = encodeURIComponent(key) + '[' + i +'].'
                                arrQueryDataParts.push(this._toQueryData(itemVal, nestedPropPrefix, includeNulls))
                            } else {
                                arrQueryDataParts.push(encodeURIComponent(key) + '[' + i +']=' + itemVal)
                            }
                        }
                    }
                    return arrQueryDataParts.filter(i => includeNulls || i !== null).join('&');
                } else {
                    return (propPrefix ? propPrefix : '') + encodeURIComponent(key) + '=' + encodeURIComponent(val)
                }
            }
          }).filter(item => item !== null).join('&');
    }
    
    _handleError(error) {
        console.log('Remote Service: error occurred.', error)
        throw error;
    }
}
    