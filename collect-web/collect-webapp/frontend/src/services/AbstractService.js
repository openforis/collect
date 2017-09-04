import Constants from '../utils/Constants';

export default class AbstractService {

    BASE_URL = Constants.API_BASE_URL;
    
    get(url, data) {
        let queryData = this._toQueryData(data);
        return fetch(this.BASE_URL + url + '?' + queryData, {
            credentials: 'same-origin'
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
        return fetch(this.BASE_URL + url, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json;charset=UTF-8'
            },
            body: JSON.stringify(data)
        }).then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
    }

    patchJson(url, data) {
        return fetch(this.BASE_URL + url, {
            method: 'PATCH',
            headers: {
              'Content-Type': 'application/json;charset=UTF-8'
            },
            body: JSON.stringify(data)
        }).then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
    }

    _toQueryData(data, propPrefix) {
        if (! data) {
            return ''
        }
        return Object.keys(data).map((key) => {
            let val = data[key];
            if (val === null) {
                return '';
            } else {
                if (val instanceof Array) {
                    let arrQueryDataParts = []
                    for(let i=0; i<val.length; i++) {
                        let nestedPropPrefix = encodeURIComponent(key) + '[' + i +'].';
                        arrQueryDataParts.push(this._toQueryData(val[i], nestedPropPrefix))
                    }
                    return arrQueryDataParts.join('&');
                } else {
                    return (propPrefix ? propPrefix : '') + encodeURIComponent(key) + '=' + encodeURIComponent(val)
                }
            }
          }).join('&');
    }

}
    