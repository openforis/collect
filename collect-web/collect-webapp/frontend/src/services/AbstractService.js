import Constants from '../utils/Constants';

export default class AbstractService {

    BASE_URL = Constants.API_BASE_URL;
    
    get(url, data) {
        let queryData = this._toQueryData(data);
        return fetch(this.BASE_URL + url + '?' + queryData, {
            credentials: 'include'
        })
        .then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
    }

    delete(url) {
        return fetch(this.BASE_URL + url, {
            credentials: 'include',
            method: 'DELETE'
        })
        .then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
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
            error => console.log('An error occured.', error))
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
            formData.append(name, data[name])
        }

        return fetch(this.BASE_URL + url, {
            credentials: 'include',
            method: 'POST',
            body: formData
        }).then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
    }

    patchJson(url, data) {
        return this._sendJson(url, data, 'PATCH')
    }

    _sendJson(url, data, method) {
        return fetch(this.BASE_URL + url, {
            credentials: 'include',
            method: method,
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
    }

    downloadFile(url, windowName) {
        window.open(url, windowName ? windowName : '_blank')
    }

    _toQueryData(data, propPrefix) {
        if (! data) {
            return ''
        }
        return Object.keys(data).map((key) => {
            const val = data[key];
            if (val === null || val === '') {
                return null;
            } else {
                if (val instanceof Array) {
                    const arr = val
                    const arrQueryDataParts = []
                    for(let i=0; i<arr.length; i++) {
                        const itemVal = arr[i]
                        if (itemVal !== null && itemVal !== undefined) {
                            if (typeof itemVal === 'object') {
                                const nestedPropPrefix = encodeURIComponent(key) + '[' + i +'].'
                                arrQueryDataParts.push(this._toQueryData(itemVal, nestedPropPrefix))
                            } else {
                                arrQueryDataParts.push(encodeURIComponent(key) + '[' + i +']=' + itemVal)
                            }
                        }
                    }
                    return arrQueryDataParts.join('&');
                } else {
                    return (propPrefix ? propPrefix : '') + encodeURIComponent(key) + '=' + encodeURIComponent(val)
                }
            }
          }).filter(item => item !== null && item !== '').join('&');
    }
    
}
    