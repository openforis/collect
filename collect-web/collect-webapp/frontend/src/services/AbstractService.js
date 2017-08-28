import Constants from '../utils/Constants';

export default class AbstractService {

    BASE_URL = Constants.API_BASE_URL;
    
    get(url) {
        return fetch(this.BASE_URL + url, {
            credentials: 'same-origin'
        })
        .then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
    }

    post(url, data) {
        let toQueryData = function(data, propPrefix) {
            return Object.keys(data).map((key) => {
                let val = data[key];
                if (val === null) {
                    return '';
                } else {
                    if (val instanceof Array) {
                        let arrQueryDataParts = []
                        for(let i=0; i<val.length; i++) {
                            let nestedPropPrefix = encodeURIComponent(key) + '[' + i +'].';
                            arrQueryDataParts.push(toQueryData(val[i], nestedPropPrefix))
                        }
                        return arrQueryDataParts.join('&');
                    } else {
                        return (propPrefix ? propPrefix : '') + encodeURIComponent(key) + '=' + encodeURIComponent(val)
                    }
                }
              }).join('&');
        }
        let body = toQueryData(data);

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
}
    